/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import TextField from '@mui/material/TextField';
import { DataGrid } from '@mui/x-data-grid';
import { queues as queuesApi, ApiError } from '../../api/client';
import IdentityCell from '../../components/IdentityCell';
import PageHeader from '../../components/PageHeader';
import StatusTag from '../../components/StatusTag';
import Toast from '../../components/Toast';
import { dataGridSx } from '../../components/DataTableShell';
import { queueStatusInfo } from '../../statusVocabulary';

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
      setToast({ severity: 'success', message: `Request ${decision === 'APPROVED' ? 'approved' : 'declined'}.` });
      setDialog(null);
      load(tab);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to update request.';
      setToast({ severity: 'error', message });
    }
  };

  const pendingCount = rows.filter((r) => r.status === 'PENDING').length;

  const columns = [
    { field: 'type', headerName: 'Type', width: 160, valueFormatter: (p) => p.value.replace('_', ' ') },
    {
      field: 'userName', headerName: 'Requester', width: 220,
      renderCell: (p) => <IdentityCell primary={p.row.userName} secondary={p.row.userEmail} id={p.row.userKeycloakId} />,
    },
    {
      field: 'bookTitle', headerName: 'Book', width: 220,
      renderCell: (p) => p.row.bookId
        ? <IdentityCell primary={p.row.bookTitle} secondary={p.row.bookIsbn} id={p.row.bookId} />
        : '—',
    },
    {
      field: 'status', headerName: 'Status', width: 190,
      renderCell: (p) => {
        const status = queueStatusInfo(p.value);
        return <StatusTag label={status.label} variant={status.variant} />;
      },
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
          <Button size="small" variant="contained" onClick={() => openDecision(p.row, 'APPROVED')}>
            Approve
          </Button>
          <Button size="small" variant="outlined" onClick={() => openDecision(p.row, 'REJECTED')}>
            Decline
          </Button>
        </Stack>
      ),
    },
  ];

  return (
    <Box>
      <PageHeader
        title="Requests"
        answer={loading ? '' : `${pendingCount} request${pendingCount === 1 ? '' : 's'} waiting on the library.`}
        controls={
          <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" scrollButtons="auto" allowScrollButtonsMobile>
            {TABS.map((t) => <Tab key={t} value={t} label={t} />)}
          </Tabs>
        }
      />

      <Box sx={{ height: 560 }}>
        <DataGrid
          rows={rows}
          columns={columns}
          loading={loading}
          disableRowSelectionOnClick
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          pageSizeOptions={[10, 25, 50]}
          sx={dataGridSx}
        />
      </Box>

      <Dialog open={Boolean(dialog)} onClose={() => setDialog(null)} fullWidth maxWidth="sm">
        <DialogTitle>{dialog?.decision === 'APPROVED' ? 'Approve request' : 'Decline request'}</DialogTitle>
        <DialogContent>
          <TextField
            label="Note (optional)"
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

      <Toast toast={toast} onClose={() => setToast(null)} />
    </Box>
  );
}
