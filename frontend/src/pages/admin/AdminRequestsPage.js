/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Chip from '@mui/material/Chip';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import TextField from '@mui/material/TextField';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import { DataGrid } from '@mui/x-data-grid';
import { queues as queuesApi, ApiError } from '../../api/client';

const STATUS_COLOR = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'error' };
const TABS = ['ALL', 'PENDING', 'APPROVED', 'REJECTED'];

export default function AdminRequestsPage() {
  const [tab, setTab] = useState('PENDING');
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialog, setDialog] = useState(null); // { item, decision }
  const [remark, setRemark] = useState('');
  const [toast, setToast] = useState(null);

  const load = async (status) => {
    setLoading(true);
    try {
      const data = await queuesApi.getAll(status === 'ALL' ? {} : { status });
      setRows(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(tab); }, [tab]);

  const openDecision = (item, decision) => {
    setDialog({ item, decision });
    setRemark('');
  };

  const submitDecision = async () => {
    const { item, decision } = dialog;
    try {
      await queuesApi.updateStatus(item.id, { status: decision, adminRemark: remark || undefined });
      setToast({ severity: 'success', message: `Request ${decision.toLowerCase()}` });
      setDialog(null);
      load(tab);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to update request';
      setToast({ severity: 'error', message });
    }
  };

  const columns = [
    { field: 'type', headerName: 'Type', width: 160, valueFormatter: (p) => p.value.replace('_', ' ') },
    { field: 'userKeycloakId', headerName: 'Requester', width: 220 },
    { field: 'bookId', headerName: 'Book ID', width: 220 },
    {
      field: 'status', headerName: 'Status', width: 130,
      renderCell: (p) => <Chip size="small" label={p.value} color={STATUS_COLOR[p.value]} />,
    },
    { field: 'description', headerName: 'Description', flex: 1, minWidth: 200 },
    {
      field: 'createdAt', headerName: 'Submitted', width: 180,
      valueFormatter: (p) => new Date(p.value).toLocaleString(),
    },
    {
      field: 'actions', headerName: '', width: 220, sortable: false, filterable: false,
      renderCell: (p) => p.row.status === 'PENDING' && (
        <Stack direction="row" spacing={1}>
          <Button size="small" variant="contained" color="success" onClick={() => openDecision(p.row, 'APPROVED')}>
            Approve
          </Button>
          <Button size="small" variant="outlined" color="error" onClick={() => openDecision(p.row, 'REJECTED')}>
            Reject
          </Button>
        </Stack>
      ),
    },
  ];

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} mb={0.5}>Requests</Typography>
      <Typography variant="body1" color="text.secondary" mb={3}>
        Approve or reject borrow, return, and registration requests.
      </Typography>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        {TABS.map((t) => <Tab key={t} value={t} label={t} />)}
      </Tabs>

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

      <Dialog open={Boolean(dialog)} onClose={() => setDialog(null)} fullWidth maxWidth="sm">
        <DialogTitle>{dialog?.decision === 'APPROVED' ? 'Approve request' : 'Reject request'}</DialogTitle>
        <DialogContent>
          <TextField
            label="Admin remark (optional)"
            value={remark}
            onChange={(e) => setRemark(e.target.value)}
            fullWidth
            multiline
            minRows={2}
            sx={{ mt: 1 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(null)}>Cancel</Button>
          <Button variant="contained" onClick={submitDecision}>Confirm</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast && <Alert severity={toast.severity} onClose={() => setToast(null)}>{toast.message}</Alert>}
      </Snackbar>
    </Box>
  );
}
