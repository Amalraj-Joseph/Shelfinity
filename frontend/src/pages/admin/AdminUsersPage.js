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
import IconButton from '@mui/material/IconButton';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import EditIcon from '@mui/icons-material/EditOutlined';
import DeleteIcon from '@mui/icons-material/DeleteOutline';
import { DataGrid } from '@mui/x-data-grid';
import { users as usersApi, ApiError } from '../../api/client';

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
      setToast({ severity: 'success', message: 'Role updated' });
      setEditTarget(null);
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to update role';
      setToast({ severity: 'error', message });
    }
  };

  const confirmDelete = async () => {
    try {
      await usersApi.remove(deleteTarget.id);
      setToast({ severity: 'success', message: 'User deleted' });
      setDeleteTarget(null);
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to delete user';
      setToast({ severity: 'error', message });
    }
  };

  const columns = [
    { field: 'name', headerName: 'Name', width: 200 },
    { field: 'email', headerName: 'Email', width: 260 },
    {
      field: 'role', headerName: 'Role', width: 130,
      renderCell: (p) => <Chip size="small" label={p.value} color={p.value === 'ADMIN' ? 'primary' : 'default'} />,
    },
    {
      field: 'active', headerName: 'Status', width: 150,
      renderCell: (p) => <Chip size="small" label={p.value ? 'Active' : 'Pending approval'} color={p.value ? 'success' : 'warning'} />,
    },
    {
      field: 'createdAt', headerName: 'Joined', width: 180,
      valueFormatter: (p) => new Date(p.value).toLocaleDateString(),
    },
    {
      field: 'actions', headerName: '', width: 120, sortable: false, filterable: false,
      renderCell: (p) => (
        <>
          <IconButton size="small" onClick={() => openEdit(p.row)}><EditIcon fontSize="small" /></IconButton>
          <IconButton size="small" color="error" onClick={() => setDeleteTarget(p.row)}><DeleteIcon fontSize="small" /></IconButton>
        </>
      ),
    },
  ];

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} mb={0.5}>Users</Typography>
      <Typography variant="body1" color="text.secondary" mb={3}>
        Manage user roles and accounts.
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
            <MenuItem value="USER">User</MenuItem>
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

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast && <Alert severity={toast.severity} onClose={() => setToast(null)}>{toast.message}</Alert>}
      </Snackbar>
    </Box>
  );
}
