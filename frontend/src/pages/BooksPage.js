/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useMemo, useState } from 'react';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import Skeleton from '@mui/material/Skeleton';
import { Search } from 'lucide-react';
import { books as booksApi, queues as queuesApi, reservations as reservationsApi, ApiError } from '../api/client';
import { useAuth } from '../context/AuthContext';
import PageHeader from '../components/PageHeader';
import BookTile from '../components/BookTile';
import EmptyState from '../components/EmptyState';
import Toast from '../components/Toast';

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
        setToast({ severity: 'success', message: `Borrow request submitted for "${book.title}".` });
      } else {
        await reservationsApi.create({ bookId: book.id, notes: `Reservation for ${book.title}` });
        setToast({ severity: 'success', message: `"${book.title}" reserved — you'll be notified when it's available.` });
      }
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Something went wrong.';
      setToast({ severity: 'error', message });
    } finally {
      setBusyBookId(null);
    }
  };

  const availableCount = allBooks.filter((b) => b.available).length;

  return (
    <Box>
      <PageHeader
        title="Browse Books"
        answer={
          loading
            ? ''
            : `${allBooks.length} ${allBooks.length === 1 ? 'title' : 'titles'} in the catalogue, ${availableCount} on the shelf right now.`
        }
        controls={
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <TextField
              placeholder="Search by title or author"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              fullWidth
              size="small"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Search size={16} strokeWidth={2} />
                  </InputAdornment>
                ),
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
        }
      />

      {loading ? (
        <Grid container spacing={3}>
          {Array.from({ length: 8 }).map((_, i) => (
            // eslint-disable-next-line react/no-array-index-key
            <Grid item xs={12} sm={6} md={4} lg={3} key={i}>
              <Skeleton variant="rectangular" height={128} />
            </Grid>
          ))}
        </Grid>
      ) : filteredBooks.length === 0 ? (
        <EmptyState
          headline={search || genre ? `Nothing matched "${search || genre}"` : 'The catalogue is empty'}
          description={search || genre ? 'Try a different title, author, or genre.' : 'Check back once titles have been added.'}
        />
      ) : (
        <Grid container spacing={3}>
          {filteredBooks.map((book) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={book.id}>
              <BookTile
                book={book}
                actionLabel={book.available ? 'Borrow this' : 'Reserve'}
                busy={busyBookId === book.id}
                disabled={isPendingApproval}
                onAction={() => handleAction(book, book.available ? 'borrow' : 'reserve')}
                testId={book.available ? `borrow-${book.id}` : `reserve-${book.id}`}
              />
            </Grid>
          ))}
        </Grid>
      )}

      <Toast toast={toast} onClose={() => setToast(null)} />
    </Box>
  );
}
