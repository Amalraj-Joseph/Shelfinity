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
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import Chip from '@mui/material/Chip';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/EditOutlined';
import DeleteIcon from '@mui/icons-material/DeleteOutline';
import UploadFileIcon from '@mui/icons-material/UploadFileOutlined';
import DownloadIcon from '@mui/icons-material/DownloadOutlined';
import { DataGrid } from '@mui/x-data-grid';
import { books as booksApi, ApiError } from '../../api/client';

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
        setToast({ severity: 'success', message: 'Book updated' });
      } else {
        await booksApi.create(payload);
        setToast({ severity: 'success', message: 'Book added' });
      }
      setFormOpen(false);
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to save book';
      setToast({ severity: 'error', message });
    }
  };

  const confirmDelete = async () => {
    try {
      await booksApi.remove(deleteTarget.id);
      setToast({ severity: 'success', message: 'Book deleted' });
      setDeleteTarget(null);
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to delete book';
      setToast({ severity: 'error', message });
    }
  };

  const handleFileSelected = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const result = await booksApi.bulkUpload(file);
      setUploadResult(result);
      setToast({ severity: 'success', message: `${result.successCount} books uploaded, ${result.errorCount} errors` });
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Bulk upload failed';
      setToast({ severity: 'error', message });
    } finally {
      e.target.value = '';
    }
  };

  const columns = [
    { field: 'title', headerName: 'Title', flex: 1, minWidth: 180 },
    { field: 'author', headerName: 'Author', width: 180 },
    { field: 'genre', headerName: 'Genre', width: 150 },
    {
      field: 'available', headerName: 'Status', width: 130,
      renderCell: (p) => <Chip size="small" label={p.value ? 'Available' : 'Unavailable'} color={p.value ? 'success' : 'default'} />,
    },
    { field: 'availableCopies', headerName: 'Available', width: 100 },
    { field: 'totalCopies', headerName: 'Total', width: 90 },
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
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" fontWeight={700}>Manage Books</Typography>
          <Typography variant="body1" color="text.secondary">Add, edit, remove, or bulk-upload the catalog.</Typography>
        </Box>
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>Add Book</Button>
      </Stack>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ sm: 'center' }}>
            <Typography variant="subtitle2" fontWeight={700}>Bulk Upload (CSV)</Typography>
            <Button
              variant="outlined"
              startIcon={<UploadFileIcon />}
              onClick={() => fileInputRef.current?.click()}
            >
              Choose CSV file
            </Button>
            <input ref={fileInputRef} type="file" accept=".csv" hidden onChange={handleFileSelected} />
            <Button
              variant="text"
              startIcon={<DownloadIcon />}
              href={booksApi.bulkUploadTemplateUrl()}
              target="_blank"
              rel="noopener"
            >
              Download template
            </Button>
          </Stack>
          {uploadResult && (
            <Typography variant="body2" color="text.secondary" mt={1}>
              Last upload: {uploadResult.successCount} succeeded, {uploadResult.errorCount} failed.
              {uploadResult.errorMessages?.length > 0 && ` (${uploadResult.errorMessages[0]})`}
            </Typography>
          )}
        </CardContent>
      </Card>

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

      <Dialog open={formOpen} onClose={() => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editingId ? 'Edit Book' : 'Add Book'}</DialogTitle>
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
              <TextField label="Publication Year" type="number" fullWidth value={form.publicationYear} onChange={(e) => setForm({ ...form, publicationYear: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Total Copies" type="number" fullWidth value={form.totalCopies} onChange={(e) => setForm({ ...form, totalCopies: e.target.value })} />
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

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast && <Alert severity={toast.severity} onClose={() => setToast(null)}>{toast.message}</Alert>}
      </Snackbar>
    </Box>
  );
}
