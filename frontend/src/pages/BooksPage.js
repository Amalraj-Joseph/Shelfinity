/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useMemo, useState } from 'react';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardActions from '@mui/material/CardActions';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import MenuItem from '@mui/material/MenuItem';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Stack from '@mui/material/Stack';
import CircularProgress from '@mui/material/CircularProgress';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import SearchIcon from '@mui/icons-material/Search';
import { books as booksApi, queues as queuesApi, reservations as reservationsApi, ApiError } from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function BooksPage() {
  const { isPendingApproval } = useAuth();
  const [allBooks, setAllBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [genre, setGenre] = useState('');
  const [toast, setToast] = useState(null);
  const [busyBookId, setBusyBookId] = useState(null);

  const loadBooks = async () => {
    setLoading(true);
    try {
      const data = await booksApi.getAll();
      setAllBooks(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadBooks(); }, []);

  const genres = useMemo(
    () => Array.from(new Set(allBooks.map((b) => b.genre).filter(Boolean))).sort(),
    [allBooks],
  );

  const filteredBooks = useMemo(() => {
    return allBooks.filter((b) => {
      const matchesGenre = !genre || b.genre === genre;
      const term = search.trim().toLowerCase();
      const matchesSearch = !term || b.title.toLowerCase().includes(term) || b.author.toLowerCase().includes(term);
      return matchesGenre && matchesSearch;
    });
  }, [allBooks, search, genre]);

  const handleAction = async (book, action) => {
    setBusyBookId(book.id);
    try {
      if (action === 'borrow') {
        await queuesApi.create({ type: 'BOOK_BORROW', bookId: book.id, description: `Borrow request for ${book.title}` });
        setToast({ severity: 'success', message: `Borrow request submitted for "${book.title}"` });
      } else {
        await reservationsApi.create({ bookId: book.id, notes: `Reservation for ${book.title}` });
        setToast({ severity: 'success', message: `"${book.title}" reserved — you'll be notified when it's available` });
      }
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Something went wrong';
      setToast({ severity: 'error', message });
    } finally {
      setBusyBookId(null);
    }
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} mb={0.5}>Browse Books</Typography>
      <Typography variant="body1" color="text.secondary" mb={3}>
        {allBooks.length} books in the catalog
      </Typography>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} mb={3}>
        <TextField
          placeholder="Search by title or author"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          fullWidth
          size="small"
          InputProps={{
            startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment>,
            'data-testid': 'book-search-input',
          }}
        />
        <TextField
          select
          label="Genre"
          value={genre}
          onChange={(e) => setGenre(e.target.value)}
          size="small"
          sx={{ minWidth: 200 }}
        >
          <MenuItem value="">All genres</MenuItem>
          {genres.map((g) => <MenuItem key={g} value={g}>{g}</MenuItem>)}
        </TextField>
      </Stack>

      {loading ? (
        <Box display="flex" justifyContent="center" mt={6}><CircularProgress /></Box>
      ) : (
        <Grid container spacing={3}>
          {filteredBooks.map((book) => (
            <Grid item xs={12} sm={6} md={4} key={book.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Stack direction="row" justifyContent="space-between" alignItems="flex-start" mb={1}>
                    <Chip
                      size="small"
                      label={book.available ? 'Available' : 'Unavailable'}
                      color={book.available ? 'success' : 'default'}
                    />
                    {book.genre && <Chip size="small" label={book.genre} variant="outlined" />}
                  </Stack>
                  <Typography variant="subtitle1" fontWeight={700}>{book.title}</Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>{book.author}</Typography>
                  {book.description && (
                    <Typography variant="body2" color="text.secondary" sx={{
                      display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical', overflow: 'hidden',
                    }}>
                      {book.description}
                    </Typography>
                  )}
                  <Typography variant="caption" color="text.secondary" display="block" mt={1}>
                    {book.availableCopies} of {book.totalCopies} copies available
                  </Typography>
                </CardContent>
                <CardActions sx={{ px: 2, pb: 2 }}>
                  {book.available ? (
                    <Button
                      fullWidth
                      variant="contained"
                      disabled={isPendingApproval || busyBookId === book.id}
                      onClick={() => handleAction(book, 'borrow')}
                      data-testid={`borrow-${book.id}`}
                    >
                      {busyBookId === book.id ? <CircularProgress size={20} color="inherit" /> : 'Request to Borrow'}
                    </Button>
                  ) : (
                    <Button
                      fullWidth
                      variant="outlined"
                      disabled={isPendingApproval || busyBookId === book.id}
                      onClick={() => handleAction(book, 'reserve')}
                      data-testid={`reserve-${book.id}`}
                    >
                      {busyBookId === book.id ? <CircularProgress size={20} /> : 'Reserve'}
                    </Button>
                  )}
                </CardActions>
              </Card>
            </Grid>
          ))}
          {filteredBooks.length === 0 && (
            <Grid item xs={12}>
              <Typography color="text.secondary" align="center" py={6}>No books match your search.</Typography>
            </Grid>
          )}
        </Grid>
      )}

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast && <Alert severity={toast.severity} onClose={() => setToast(null)}>{toast.message}</Alert>}
      </Snackbar>
    </Box>
  );
}
