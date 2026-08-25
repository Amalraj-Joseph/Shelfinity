/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Table from '@mui/material/Table';
import TableHead from '@mui/material/TableHead';
import TableBody from '@mui/material/TableBody';
import TableRow from '@mui/material/TableRow';
import TableCell from '@mui/material/TableCell';
import Button from '@mui/material/Button';
import Skeleton from '@mui/material/Skeleton';
import { reservations as reservationsApi, ApiError } from '../../api/client';
import IdentityCell from '../../components/IdentityCell';
import PageHeader from '../../components/PageHeader';
import StatusTag from '../../components/StatusTag';
import EmptyState from '../../components/EmptyState';
import Toast from '../../components/Toast';
import { TableShell, tableShellSx, tableRowSx } from '../../components/DataTableShell';
import { reservationStatusInfo } from '../../statusVocabulary';

function ReservationTable({ rows, action }) {
  return (
    <TableShell sx={tableShellSx}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Book</TableCell>
            <TableCell>Member</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Expires</TableCell>
            {action && <TableCell align="right">Action</TableCell>}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((r) => {
            const status = reservationStatusInfo(r.status);
            return (
              <TableRow key={r.id} sx={tableRowSx}>
                <TableCell><IdentityCell primary={r.bookTitle} secondary={r.bookIsbn} id={r.bookId} /></TableCell>
                <TableCell><IdentityCell primary={r.userName} secondary={r.userEmail} id={r.userKeycloakId} /></TableCell>
                <TableCell><StatusTag label={status.label} variant={status.variant} /></TableCell>
                <TableCell>{r.expiresAt ? new Date(r.expiresAt).toLocaleDateString() : '—'}</TableCell>
                {action && <TableCell align="right">{action(r)}</TableCell>}
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </TableShell>
  );
}

export default function AdminReservationsPage() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);
  const [busyId, setBusyId] = useState(null);

  const load = async () => {
    setLoading(true);
    try {
      setRows(await reservationsApi.getAll());
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const fulfill = async (id) => {
    setBusyId(id);
    try {
      await reservationsApi.fulfill(id);
      setToast({ severity: 'success', message: 'Reservation marked fulfilled.' });
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to fulfill reservation.';
      setToast({ severity: 'error', message });
    } finally {
      setBusyId(null);
    }
  };

  if (loading) {
    return (
      <Box>
        <Skeleton width={260} height={48} sx={{ mb: 1 }} />
        <Skeleton width={360} height={28} sx={{ mb: 3 }} />
        <Skeleton height={2} sx={{ mb: 4 }} />
        <Skeleton height={140} sx={{ mb: 4 }} />
        <Skeleton height={140} />
      </Box>
    );
  }

  const waiting = rows.filter((r) => r.status === 'ACTIVE');
  const readyForPickup = rows.filter((r) => r.status === 'NOTIFIED');
  const past = rows.filter((r) => ['FULFILLED', 'CANCELLED', 'EXPIRED'].includes(r.status));

  return (
    <Box>
      <PageHeader
        title="Reservations"
        answer={`${waiting.length} waiting on a copy, ${readyForPickup.length} ready for pickup.`}
      />

      <Box sx={{ mb: 4 }}>
        <Typography sx={{ fontSize: 20, fontWeight: 800, mb: 2 }}>Ready for pickup</Typography>
        {readyForPickup.length === 0 ? (
          <EmptyState headline="Nothing to hand over" description="Reservations move here once a copy is free and the member's been notified." />
        ) : (
          <ReservationTable
            rows={readyForPickup}
            action={(r) => (
              <Button size="small" variant="contained" disabled={busyId === r.id} onClick={() => fulfill(r.id)}>
                Mark fulfilled
              </Button>
            )}
          />
        )}
      </Box>

      <Box sx={{ mb: 4 }}>
        <Typography sx={{ fontSize: 20, fontWeight: 800, mb: 2 }}>Waiting on a copy</Typography>
        {waiting.length === 0 ? (
          <EmptyState headline="No one is waiting" description="A reservation shows here once every copy of a title is out." />
        ) : (
          <ReservationTable rows={waiting} />
        )}
      </Box>

      {past.length > 0 && (
        <Box>
          <Typography sx={{ fontSize: 20, fontWeight: 800, mb: 2 }}>Past</Typography>
          <ReservationTable rows={past} />
        </Box>
      )}

      <Toast toast={toast} onClose={() => setToast(null)} />
    </Box>
  );
}
