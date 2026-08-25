/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import IconButton from '@mui/material/IconButton';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import TextField from '@mui/material/TextField';
import Grid from '@mui/material/Grid';
import FormControlLabel from '@mui/material/FormControlLabel';
import Switch from '@mui/material/Switch';
import Skeleton from '@mui/material/Skeleton';
import { Plus, Pencil, Trash2, Power, Send } from 'lucide-react';
import { emailConfig as emailConfigApi, ApiError } from '../../api/client';
import PageHeader from '../../components/PageHeader';
import StatusTag from '../../components/StatusTag';
import EmptyState from '../../components/EmptyState';
import Toast from '../../components/Toast';
import { colors } from '../../theme/tokens';

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
      setToast({ severity: 'success', message: 'Email configuration saved.' });
      setFormOpen(false);
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to save configuration.';
      setToast({ severity: 'error', message });
    }
  };

  const activate = async (id) => {
    try {
      await emailConfigApi.activate(id);
      setToast({ severity: 'success', message: 'Configuration activated.' });
      load();
    } catch (err) {
      setToast({ severity: 'error', message: err instanceof ApiError ? err.message : 'Failed to activate.' });
    }
  };

  const remove = async (id) => {
    try {
      await emailConfigApi.remove(id);
      setToast({ severity: 'success', message: 'Configuration deleted.' });
      load();
    } catch (err) {
      setToast({ severity: 'error', message: err instanceof ApiError ? err.message : 'Failed to delete.' });
    }
  };

  const sendTest = async () => {
    try {
      await emailConfigApi.test(testEmail);
      setToast({ severity: 'success', message: 'Test email sent.' });
      setTestOpen(false);
    } catch (err) {
      setToast({ severity: 'error', message: err instanceof ApiError ? err.message : 'Test email failed.' });
    }
  };

  const activeConfig = configs.find((c) => c.active);

  if (loading) {
    return (
      <Box>
        <Skeleton width={320} height={48} sx={{ mb: 1 }} />
        <Skeleton width={420} height={28} sx={{ mb: 3 }} />
        <Skeleton height={2} sx={{ mb: 4 }} />
        <Skeleton height={140} />
      </Box>
    );
  }

  return (
    <Box>
      <PageHeader
        title="Email & notifications"
        answer={activeConfig
          ? `Email is set up through ${activeConfig.smtpHost} and active.`
          : "Email isn't set up, so members aren't being told when a hold is ready."}
        actions={
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
            <Button variant="outlined" startIcon={<Send size={16} />} onClick={() => setTestOpen(true)}>Send test email</Button>
            <Button variant="contained" startIcon={<Plus size={16} />} onClick={openCreate}>Add configuration</Button>
          </Stack>
        }
      />

      {configs.length === 0 ? (
        <EmptyState
          headline="No email configuration yet"
          description="Add an SMTP configuration so members get notified about holds, approvals, and overdue reminders."
        />
      ) : (
        <Grid container spacing={3}>
          {configs.map((config) => (
            <Grid item xs={12} md={6} key={config.id}>
              <Box sx={{ border: `1px solid ${colors.divider}`, bgcolor: colors.surface, p: 2.5 }}>
                <Stack direction="row" justifyContent="space-between" alignItems="flex-start" mb={1}>
                  <Box>
                    <Typography sx={{ fontSize: 16, fontWeight: 800 }}>{config.smtpHost}:{config.smtpPort}</Typography>
                    <Typography sx={{ fontSize: 13, color: colors.textMuted }}>{config.senderEmail}</Typography>
                  </Box>
                  <StatusTag label={config.active ? 'Active' : 'Inactive'} variant={config.active ? 'neutral' : 'outline'} />
                </Stack>
                <Stack direction="row" spacing={1} mt={1}>
                  {config.useTls && <StatusTag label="TLS" variant="outline" />}
                  {config.useSsl && <StatusTag label="SSL" variant="outline" />}
                  {config.hasPassword && <StatusTag label="Password set" variant="outline" />}
                </Stack>
                <Stack direction="row" spacing={0.5} mt={2} alignItems="center">
                  {!config.active && (
                    <Button size="small" startIcon={<Power size={14} />} onClick={() => activate(config.id)}>
                      Activate
                    </Button>
                  )}
                  <IconButton size="small" onClick={() => openEdit(config)} aria-label="Edit configuration">
                    <Pencil size={16} />
                  </IconButton>
                  <IconButton size="small" onClick={() => remove(config.id)} aria-label="Delete configuration">
                    <Trash2 size={16} />
                  </IconButton>
                </Stack>
              </Box>
            </Grid>
          ))}
        </Grid>
      )}

      <Dialog open={formOpen} onClose={() => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editingId ? 'Edit configuration' : 'Add configuration'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} mt={0.5}>
            <Grid item xs={8}>
              <TextField label="SMTP host" fullWidth value={form.smtpHost} onChange={(e) => setForm({ ...form, smtpHost: e.target.value })} />
            </Grid>
            <Grid item xs={4}>
              <TextField label="Port" type="number" fullWidth value={form.smtpPort} onChange={(e) => setForm({ ...form, smtpPort: e.target.value })} />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Sender email" fullWidth value={form.senderEmail} onChange={(e) => setForm({ ...form, senderEmail: e.target.value })} />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Sender name" fullWidth value={form.senderName} onChange={(e) => setForm({ ...form, senderName: e.target.value })} />
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
        <DialogTitle>Send test email</DialogTitle>
        <DialogContent>
          <TextField label="Send to" fullWidth value={testEmail} onChange={(e) => setTestEmail(e.target.value)} sx={{ mt: 1 }} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTestOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={sendTest}>Send</Button>
        </DialogActions>
      </Dialog>

      <Toast toast={toast} onClose={() => setToast(null)} />
    </Box>
  );
}
