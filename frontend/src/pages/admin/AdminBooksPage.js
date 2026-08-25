/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useRef, useState } from 'react';
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
import { DataGrid } from '@mui/x-data-grid';
import { Plus, Pencil, Trash2, Upload, Download } from 'lucide-react';
import { books as booksApi, ApiError } from '../../api/client';
import PageHeader from '../../components/PageHeader';
import StatusTag from '../../components/StatusTag';
import Toast from '../../components/Toast';
import { dataGridSx } from '../../components/DataTableShell';
import { colors } from '../../theme/tokens';

const EMPTY_FORM = { title: '', author: '', isbn: '', description: '', genre: '', publicationYear: '', totalCopies: 1 };

export default function AdminBooksPage() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [toast, setToast] = useState(null);
  const [uploadResult, setUploadResult] = useState(null);
  const fileInputRef = useRef(null);

  const load = async () => {
    setLoading(true);
    try {
      setRows(await booksApi.getAll());
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

  const openEdit = (book) => {
    setEditingId(book.id);
    setForm({
      title: book.title, author: book.author, isbn: book.isbn || '',
      description: book.description || '', genre: book.genre || '',
      publicationYear: book.publicationYear || '', totalCopies: book.totalCopies,
    });
    setFormOpen(true);
  };

  const submitForm = async () => {
    const payload = {
      ...form,
      totalCopies: Number(form.totalCopies) || 1,
      publicationYear: form.publicationYear ? Number(form.publicationYear) : undefined,
    };
    try {
      if (editingId) {
        await booksApi.update(editingId, payload);
        setToast({ severity: 'success', message: 'Book updated.' });
      } else {
        await booksApi.create(payload);
        setToast({ severity: 'success', message: 'Book added.' });
      }
      setFormOpen(false);
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to save book.';
      setToast({ severity: 'error', message });
    }
  };

  const confirmDelete = async () => {
    try {
      await booksApi.remove(deleteTarget.id);
      setToast({ severity: 'success', message: 'Book deleted.' });
      setDeleteTarget(null);
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to delete book.';
      setToast({ severity: 'error', message });
    }
  };

  const handleFileSelected = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const result = await booksApi.bulkUpload(file);
      setUploadResult(result);
      setToast({ severity: 'success', message: `${result.successCount} books uploaded, ${result.errorCount} errors.` });
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Bulk upload failed.';
      setToast({ severity: 'error', message });
    } finally {
      e.target.value = '';
    }
  };

  const onShelfCount = rows.filter((b) => b.available).length;

  const columns = [
    { field: 'title', headerName: 'Title', flex: 1, minWidth: 180, renderCell: (p) => <span style={{ fontWeight: 600 }}>{p.value}</span> },
    { field: 'author', headerName: 'Author', width: 180 },
    { field: 'genre', headerName: 'Genre', width: 150 },
    {
      field: 'available', headerName: 'Status', width: 140,
      renderCell: (p) => <StatusTag label={p.value ? 'On the shelf' : 'All out'} variant={p.value ? 'neutral' : 'outline'} />,
    },
    { field: 'availableCopies', headerName: 'Available', width: 100 },
    { field: 'totalCopies', headerName: 'Total', width: 90 },
    {
      field: 'actions', headerName: '', width: 100, sortable: false, filterable: false,
      renderCell: (p) => (
        <>
          <IconButton size="small" onClick={() => openEdit(p.row)} aria-label={`Edit ${p.row.title}`}>
            <Pencil size={16} />
          </IconButton>
          <IconButton size="small" onClick={() => setDeleteTarget(p.row)} aria-label={`Delete ${p.row.title}`}>
            <Trash2 size={16} />
          </IconButton>
        </>
      ),
    },
  ];

  return (
    <Box>
      <PageHeader
        title="Catalogue"
        answer={loading ? '' : `${rows.length} title${rows.length === 1 ? '' : 's'} in the catalogue, ${onShelfCount} on the shelf right now.`}
        actions={
          <Button variant="contained" startIcon={<Plus size={16} />} onClick={openCreate}>Add a title</Button>
        }
      />

      <Box sx={{ border: `1px solid ${colors.divider}`, bgcolor: colors.surface, p: 2.5, mb: 3 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ sm: 'center' }}>
          <Typography sx={{ fontSize: 13, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            Bulk import (CSV)
          </Typography>
          <Button variant="outlined" startIcon={<Upload size={16} />} onClick={() => fileInputRef.current?.click()}>
            Choose CSV file
          </Button>
          <input ref={fileInputRef} type="file" accept=".csv" hidden onChange={handleFileSelected} />
          <Button
            variant="text"
            startIcon={<Download size={16} />}
            href={booksApi.bulkUploadTemplateUrl()}
            target="_blank"
            rel="noopener"
          >
            Download template
          </Button>
        </Stack>
        {uploadResult && (
          <Typography sx={{ fontSize: 13, color: colors.textMuted, mt: 1.5 }}>
            Last import: {uploadResult.successCount} succeeded, {uploadResult.errorCount} failed.
            {uploadResult.errorMessages?.length > 0 && ` (${uploadResult.errorMessages[0]})`}
          </Typography>
        )}
      </Box>

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

      <Dialog open={formOpen} onClose={() => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editingId ? 'Edit title' : 'Add a title'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} mt={0.5}>
            <Grid item xs={12}>
              <TextField label="Title" fullWidth value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Author" fullWidth value={form.author} onChange={(e) => setForm({ ...form, author: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="ISBN" fullWidth value={form.isbn} onChange={(e) => setForm({ ...form, isbn: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Genre" fullWidth value={form.genre} onChange={(e) => setForm({ ...form, genre: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Publication year" type="number" fullWidth value={form.publicationYear} onChange={(e) => setForm({ ...form, publicationYear: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Total copies" type="number" fullWidth value={form.totalCopies} onChange={(e) => setForm({ ...form, totalCopies: e.target.value })} />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Description" fullWidth multiline minRows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFormOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={submitForm}>{editingId ? 'Save' : 'Add'}</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(deleteTarget)} onClose={() => setDeleteTarget(null)}>
        <DialogTitle>Delete &quot;{deleteTarget?.title}&quot;?</DialogTitle>
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
