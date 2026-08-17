/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Chip from '@mui/material/Chip';
import IconButton from '@mui/material/IconButton';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import TextField from '@mui/material/TextField';
import Grid from '@mui/material/Grid';
import FormControlLabel from '@mui/material/FormControlLabel';
import Switch from '@mui/material/Switch';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/EditOutlined';
import DeleteIcon from '@mui/icons-material/DeleteOutline';
import PowerSettingsNewIcon from '@mui/icons-material/PowerSettingsNewOutlined';
import SendIcon from '@mui/icons-material/SendOutlined';
import { emailConfig as emailConfigApi, ApiError } from '../../api/client';

const EMPTY_FORM = {
  smtpHost: '', smtpPort: 587, senderEmail: '', senderName: '', username: '', password: '',
  useTls: true, useSsl: false, requireAuth: true,
};

export default function AdminEmailConfigPage() {
  const [configs, setConfigs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [testOpen, setTestOpen] = useState(false);
  const [testEmail, setTestEmail] = useState('');
  const [toast, setToast] = useState(null);

  const load = async () => {
    setLoading(true);
    try {
      setConfigs(await emailConfigApi.getAll());
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const openCreate = () => {
    setEditingId(null);
    setForm(EMPTY_FORM);
    setFormOpen(true);
  };

  const openEdit = (config) => {
    setEditingId(config.id);
    setForm({
      smtpHost: config.smtpHost, smtpPort: config.smtpPort, senderEmail: config.senderEmail,
      senderName: config.senderName || '', username: config.username || '', password: '',
      useTls: config.useTls, useSsl: config.useSsl, requireAuth: config.requireAuth,
    });
    setFormOpen(true);
  };

  const submitForm = async () => {
    const payload = { ...form, smtpPort: Number(form.smtpPort) || 587 };
    if (!payload.password) delete payload.password; // preserve existing on edit
    try {
      if (editingId) {
        await emailConfigApi.update(editingId, payload);
      } else {
        await emailConfigApi.save(payload);
      }
      setToast({ severity: 'success', message: 'Email configuration saved' });
      setFormOpen(false);
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to save configuration';
      setToast({ severity: 'error', message });
    }
  };

  const activate = async (id) => {
    try {
      await emailConfigApi.activate(id);
      setToast({ severity: 'success', message: 'Configuration activated' });
      load();
    } catch (err) {
      setToast({ severity: 'error', message: err instanceof ApiError ? err.message : 'Failed to activate' });
    }
  };

  const remove = async (id) => {
    try {
      await emailConfigApi.remove(id);
      setToast({ severity: 'success', message: 'Configuration deleted' });
      load();
    } catch (err) {
      setToast({ severity: 'error', message: err instanceof ApiError ? err.message : 'Failed to delete' });
    }
  };

  const sendTest = async () => {
    try {
      await emailConfigApi.test(testEmail);
      setToast({ severity: 'success', message: 'Test email sent' });
      setTestOpen(false);
    } catch (err) {
      setToast({ severity: 'error', message: err instanceof ApiError ? err.message : 'Test email failed' });
    }
  };

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" fontWeight={700}>Email Configuration</Typography>
          <Typography variant="body1" color="text.secondary">Manage SMTP settings used for notification emails.</Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" startIcon={<SendIcon />} onClick={() => setTestOpen(true)}>Send Test Email</Button>
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>Add Configuration</Button>
        </Stack>
      </Stack>

      {!loading && configs.length === 0 && (
        <Typography color="text.secondary">No email configuration set up yet.</Typography>
      )}

      <Grid container spacing={3}>
        {configs.map((config) => (
          <Grid item xs={12} md={6} key={config.id}>
            <Card>
              <CardContent>
                <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                  <Box>
                    <Typography variant="subtitle1" fontWeight={700}>{config.smtpHost}:{config.smtpPort}</Typography>
                    <Typography variant="body2" color="text.secondary">{config.senderEmail}</Typography>
                  </Box>
                  <Chip size="small" label={config.active ? 'Active' : 'Inactive'} color={config.active ? 'success' : 'default'} />
                </Stack>
                <Stack direction="row" spacing={1} mt={1}>
                  {config.useTls && <Chip size="small" label="TLS" variant="outlined" />}
                  {config.useSsl && <Chip size="small" label="SSL" variant="outlined" />}
                  {config.hasPassword && <Chip size="small" label="Password set" variant="outlined" />}
                </Stack>
                <Stack direction="row" spacing={1} mt={2}>
                  {!config.active && (
                    <Button size="small" startIcon={<PowerSettingsNewIcon />} onClick={() => activate(config.id)}>
                      Activate
                    </Button>
                  )}
                  <IconButton size="small" onClick={() => openEdit(config)}><EditIcon fontSize="small" /></IconButton>
                  <IconButton size="small" color="error" onClick={() => remove(config.id)}><DeleteIcon fontSize="small" /></IconButton>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Dialog open={formOpen} onClose={() => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editingId ? 'Edit Configuration' : 'Add Configuration'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} mt={0.5}>
            <Grid item xs={8}>
              <TextField label="SMTP Host" fullWidth value={form.smtpHost} onChange={(e) => setForm({ ...form, smtpHost: e.target.value })} />
            </Grid>
            <Grid item xs={4}>
              <TextField label="Port" type="number" fullWidth value={form.smtpPort} onChange={(e) => setForm({ ...form, smtpPort: e.target.value })} />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Sender Email" fullWidth value={form.senderEmail} onChange={(e) => setForm({ ...form, senderEmail: e.target.value })} />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Sender Name" fullWidth value={form.senderName} onChange={(e) => setForm({ ...form, senderName: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Username" fullWidth value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField
                label={editingId ? 'Password (leave blank to keep)' : 'Password'}
                type="password"
                fullWidth
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
              />
            </Grid>
            <Grid item xs={4}>
              <FormControlLabel control={<Switch checked={form.useTls} onChange={(e) => setForm({ ...form, useTls: e.target.checked })} />} label="TLS" />
            </Grid>
            <Grid item xs={4}>
              <FormControlLabel control={<Switch checked={form.useSsl} onChange={(e) => setForm({ ...form, useSsl: e.target.checked })} />} label="SSL" />
            </Grid>
            <Grid item xs={4}>
              <FormControlLabel control={<Switch checked={form.requireAuth} onChange={(e) => setForm({ ...form, requireAuth: e.target.checked })} />} label="Auth" />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFormOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={submitForm}>Save</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={testOpen} onClose={() => setTestOpen(false)} fullWidth maxWidth="xs">
        <DialogTitle>Send Test Email</DialogTitle>
        <DialogContent>
          <TextField label="Send to" fullWidth value={testEmail} onChange={(e) => setTestEmail(e.target.value)} sx={{ mt: 1 }} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTestOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={sendTest}>Send</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast && <Alert severity={toast.severity} onClose={() => setToast(null)}>{toast.message}</Alert>}
      </Snackbar>
    </Box>
  );
}
