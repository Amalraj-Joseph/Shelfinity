/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Chip from '@mui/material/Chip';
import Button from '@mui/material/Button';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import { DataGrid } from '@mui/x-data-grid';
import { reservations as reservationsApi, ApiError } from '../../api/client';

const STATUS_COLOR = { ACTIVE: 'info', NOTIFIED: 'success', FULFILLED: 'default', CANCELLED: 'default', EXPIRED: 'default' };

export default function AdminReservationsPage() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);

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
    try {
      await reservationsApi.fulfill(id);
      setToast({ severity: 'success', message: 'Reservation marked fulfilled' });
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to fulfill reservation';
      setToast({ severity: 'error', message });
    }
  };

  const columns = [
    { field: 'bookTitle', headerName: 'Book', flex: 1, minWidth: 180 },
    { field: 'userKeycloakId', headerName: 'User', width: 220 },
    {
      field: 'status', headerName: 'Status', width: 130,
      renderCell: (p) => <Chip size="small" label={p.value} color={STATUS_COLOR[p.value]} />,
    },
    {
      field: 'expiresAt', headerName: 'Expires', width: 180,
      valueFormatter: (p) => p.value ? new Date(p.value).toLocaleString() : '—',
    },
    {
      field: 'actions', headerName: '', width: 150, sortable: false, filterable: false,
      renderCell: (p) => p.row.status === 'NOTIFIED' && (
        <Button size="small" variant="contained" onClick={() => fulfill(p.row.id)}>Mark Fulfilled</Button>
      ),
    },
  ];

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} mb={0.5}>Reservations</Typography>
      <Typography variant="body1" color="text.secondary" mb={3}>
        All active and past reservations across the library.
      </Typography>

      <Box sx={{ height: 560, bgcolor: 'background.paper', borderRadius: 2 }}>
        <DataGrid
          rows={rows}
          columns={columns}
          loading={loading}
          disableRowSelectionOnClick
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          pageSizeOptions={[10, 25, 50]}
        />
      </Box>

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast && <Alert severity={toast.severity} onClose={() => setToast(null)}>{toast.message}</Alert>}
      </Snackbar>
    </Box>
  );
}
