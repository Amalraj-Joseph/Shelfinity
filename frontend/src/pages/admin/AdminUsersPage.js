/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import { DataGrid } from '@mui/x-data-grid';
import { Pencil, Trash2 } from 'lucide-react';
import { users as usersApi, ApiError } from '../../api/client';
import PageHeader from '../../components/PageHeader';
import StatusTag from '../../components/StatusTag';
import Toast from '../../components/Toast';
import { dataGridSx } from '../../components/DataTableShell';
import { roleInfo, userStatusInfo } from '../../statusVocabulary';

export default function AdminUsersPage() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editTarget, setEditTarget] = useState(null);
  const [editRole, setEditRole] = useState('USER');
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [toast, setToast] = useState(null);

  const load = async () => {
    setLoading(true);
    try {
      setRows(await usersApi.getAll());
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const openEdit = (user) => {
    setEditTarget(user);
    setEditRole(user.role);
  };

  const saveRole = async () => {
    try {
      await usersApi.update(editTarget.id, {
        keycloakId: editTarget.keycloakId,
        email: editTarget.email,
        name: editTarget.name,
        role: editRole,
      });
      setToast({ severity: 'success', message: 'Role updated.' });
      setEditTarget(null);
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to update role.';
      setToast({ severity: 'error', message });
    }
  };

  const confirmDelete = async () => {
    try {
      await usersApi.remove(deleteTarget.id);
      setToast({ severity: 'success', message: 'User deleted.' });
      setDeleteTarget(null);
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to delete user.';
      setToast({ severity: 'error', message });
    }
  };

  const pendingCount = rows.filter((u) => !u.active).length;

  const columns = [
    { field: 'name', headerName: 'Name', width: 200, renderCell: (p) => <span style={{ fontWeight: 600 }}>{p.value}</span> },
    { field: 'email', headerName: 'Email', width: 260 },
    {
      field: 'role', headerName: 'Role', width: 110,
      renderCell: (p) => {
        const r = roleInfo(p.value);
        return <StatusTag label={r.label} variant={r.variant} />;
      },
    },
    {
      field: 'active', headerName: 'Status', width: 160,
      renderCell: (p) => {
        const s = userStatusInfo(p.value);
        return <StatusTag label={s.label} variant={s.variant} />;
      },
    },
    {
      field: 'createdAt', headerName: 'Joined', width: 140,
      valueFormatter: (p) => new Date(p.value).toLocaleDateString(),
    },
    {
      field: 'actions', headerName: '', width: 100, sortable: false, filterable: false,
      renderCell: (p) => (
        <>
          <IconButton size="small" onClick={() => openEdit(p.row)} aria-label={`Edit ${p.row.name}`}>
            <Pencil size={16} />
          </IconButton>
          <IconButton size="small" onClick={() => setDeleteTarget(p.row)} aria-label={`Delete ${p.row.name}`}>
            <Trash2 size={16} />
          </IconButton>
        </>
      ),
    },
  ];

  return (
    <Box>
      <PageHeader
        title="Users"
        answer={loading ? '' : `${rows.length} member${rows.length === 1 ? '' : 's'}, ${pendingCount} waiting on approval.`}
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

      <Dialog open={Boolean(editTarget)} onClose={() => setEditTarget(null)} fullWidth maxWidth="xs">
        <DialogTitle>Edit role — {editTarget?.name}</DialogTitle>
        <DialogContent>
          <TextField
            select
            label="Role"
            value={editRole}
            onChange={(e) => setEditRole(e.target.value)}
            fullWidth
            sx={{ mt: 1 }}
          >
            <MenuItem value="USER">Member</MenuItem>
            <MenuItem value="ADMIN">Admin</MenuItem>
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditTarget(null)}>Cancel</Button>
          <Button variant="contained" onClick={saveRole}>Save</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(deleteTarget)} onClose={() => setDeleteTarget(null)}>
        <DialogTitle>Delete {deleteTarget?.name}?</DialogTitle>
        <DialogContent>This cannot be undone.</DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteTarget(null)}>Cancel</Button>
          <Button color="error" variant="contained" onClick={confirmDelete}>Delete</Button>
        </DialogActions>
      </Dialog>

      <Toast toast={toast} onClose={() => setToast(null)} />
    </Box>
  );
}
