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
import { queues as queuesApi, reservations as reservationsApi, books as booksApi, ApiError } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatusTag from '../components/StatusTag';
import DueMeter from '../components/DueMeter';
import EmptyState from '../components/EmptyState';
import Toast from '../components/Toast';
import { TableShell, tableShellSx, tableRowSx } from '../components/DataTableShell';
import { queueStatusInfo, reservationStatusInfo } from '../statusVocabulary';

export default function MyActivityPage() {
  const [loading, setLoading] = useState(true);
  const [queueItems, setQueueItems] = useState([]);
  const [reservationList, setReservationList] = useState([]);
  const [bookTitles, setBookTitles] = useState({});
  const [toast, setToast] = useState(null);
  const [busyId, setBusyId] = useState(null);

  const load = async () => {
    setLoading(true);
    try {
      const [myQueue, myReservations] = await Promise.all([queuesApi.getMine(), reservationsApi.getMine()]);
      setQueueItems(myQueue);
      setReservationList(myReservations);

      const bookIds = Array.from(new Set(myQueue.map((q) => q.bookId).filter(Boolean)));
      const entries = await Promise.all(bookIds.map(async (id) => {
        try {
          const book = await booksApi.getById(id);
          return [id, book.title];
        } catch {
          return [id, 'Unknown book'];
        }
      }));
      setBookTitles(Object.fromEntries(entries));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const requestReturn = async (item) => {
    setBusyId(item.id);
    try {
      await queuesApi.create({
        type: 'BOOK_RETURN',
        bookId: item.bookId,
        description: `Return request for ${bookTitles[item.bookId] || 'book'}`,
      });
      setToast({ severity: 'success', message: 'Return request submitted.' });
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Something went wrong.';
      setToast({ severity: 'error', message });
    } finally {
      setBusyId(null);
    }
  };

  const cancelReservation = async (reservation) => {
    setBusyId(reservation.id);
    try {
      await reservationsApi.cancel(reservation.id);
      setToast({ severity: 'success', message: 'Reservation cancelled.' });
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Something went wrong.';
      setToast({ severity: 'error', message });
    } finally {
      setBusyId(null);
    }
  };

  const canRequestReturn = (item) => item.type === 'BOOK_BORROW' && item.status === 'APPROVED';

  if (loading) {
    return (
      <Box>
        <Skeleton width={220} height={48} sx={{ mb: 1 }} />
        <Skeleton width={360} height={28} sx={{ mb: 3 }} />
        <Skeleton height={2} sx={{ mb: 4 }} />
        <Skeleton height={160} sx={{ mb: 4 }} />
        <Skeleton height={120} />
      </Box>
    );
  }

  const dueSoonCount = queueItems.filter((q) => canRequestReturn(q) && q.dueDate).length;
  const pendingCount = queueItems.filter((q) => q.status === 'PENDING').length;
  const holdsWaiting = reservationList.filter((r) => r.status === 'ACTIVE' || r.status === 'NOTIFIED').length;
  const answer = dueSoonCount > 0 || pendingCount > 0 || holdsWaiting > 0
    ? [
      dueSoonCount > 0 && `${dueSoonCount} book${dueSoonCount === 1 ? '' : 's'} out`,
      pendingCount > 0 && `${pendingCount} request${pendingCount === 1 ? '' : 's'} waiting`,
      holdsWaiting > 0 && `${holdsWaiting} on hold`,
    ].filter(Boolean).join(', ') + '.'
    : 'Nothing out, nothing waiting.';

  return (
    <Box>
      <PageHeader title="My Shelf" answer={answer.charAt(0).toUpperCase() + answer.slice(1)} />

      <Box sx={{ mb: 4 }}>
        <Typography sx={{ fontSize: 20, fontWeight: 800, mb: 2 }}>Borrow &amp; return requests</Typography>
        {queueItems.length === 0 ? (
          <EmptyState
            headline="Nothing requested yet"
            description="Borrow a book from the catalogue and it'll show up here while the library reviews it."
          />
        ) : (
          <TableShell sx={tableShellSx}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Book</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Due</TableCell>
                  <TableCell align="right">Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {queueItems.map((item) => {
                  const status = queueStatusInfo(item.status);
                  return (
                    <TableRow key={item.id} sx={tableRowSx}>
                      <TableCell sx={{ fontWeight: 600 }}>{bookTitles[item.bookId] || '—'}</TableCell>
                      <TableCell sx={{ textTransform: 'capitalize' }}>{item.type.replace('_', ' ').toLowerCase()}</TableCell>
                      <TableCell><StatusTag label={status.label} variant={status.variant} /></TableCell>
                      <TableCell>
                        {canRequestReturn(item) && item.dueDate
                          ? <DueMeter startDate={item.processedAt} dueDate={item.dueDate} />
                          : '—'}
                      </TableCell>
                      <TableCell align="right">
                        {canRequestReturn(item) && (
                          <Button
                            size="small"
                            variant="outlined"
                            disabled={busyId === item.id}
                            onClick={() => requestReturn(item)}
                          >
                            Request return
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableShell>
        )}
      </Box>

      <Box>
        <Typography sx={{ fontSize: 20, fontWeight: 800, mb: 2 }}>Reservations</Typography>
        {reservationList.length === 0 ? (
          <EmptyState
            headline="Nothing reserved yet"
            description="Reserve any title that's all out and we'll hold a copy for you at the desk."
          />
        ) : (
          <TableShell sx={tableShellSx}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Book</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Expires</TableCell>
                  <TableCell align="right">Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {reservationList.map((r) => {
                  const status = reservationStatusInfo(r.status);
                  return (
                    <TableRow key={r.id} sx={tableRowSx}>
                      <TableCell sx={{ fontWeight: 600 }}>{r.bookTitle}</TableCell>
                      <TableCell><StatusTag label={status.label} variant={status.variant} /></TableCell>
                      <TableCell>{r.expiresAt ? new Date(r.expiresAt).toLocaleDateString() : '—'}</TableCell>
                      <TableCell align="right">
                        {(r.status === 'ACTIVE' || r.status === 'NOTIFIED') && (
                          <Button
                            size="small"
                            disabled={busyId === r.id}
                            onClick={() => cancelReservation(r)}
                            sx={{ color: 'error.main' }}
                          >
                            Cancel
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableShell>
        )}
      </Box>

      <Toast toast={toast} onClose={() => setToast(null)} />
    </Box>
  );
}
