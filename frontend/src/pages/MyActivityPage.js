/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Table from '@mui/material/Table';
import TableHead from '@mui/material/TableHead';
import TableBody from '@mui/material/TableBody';
import TableRow from '@mui/material/TableRow';
import TableCell from '@mui/material/TableCell';
import Chip from '@mui/material/Chip';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import { queues as queuesApi, reservations as reservationsApi, books as booksApi, ApiError } from '../api/client';

const STATUS_COLOR = {
  PENDING: 'warning', APPROVED: 'success', REJECTED: 'error',
  ACTIVE: 'info', NOTIFIED: 'success', FULFILLED: 'default', CANCELLED: 'default', EXPIRED: 'default',
};

export default function MyActivityPage() {
  const [loading, setLoading] = useState(true);
  const [queueItems, setQueueItems] = useState([]);
  const [reservationList, setReservationList] = useState([]);
  const [bookTitles, setBookTitles] = useState({});
  const [toast, setToast] = useState(null);
  const [busyId, setBusyId] = useState(null);

  const load = async () => {
    setLoading(true);
    try {
      const [myQueue, myReservations] = await Promise.all([queuesApi.getMine(), reservationsApi.getMine()]);
      setQueueItems(myQueue);
      setReservationList(myReservations);

      const bookIds = Array.from(new Set(myQueue.map((q) => q.bookId).filter(Boolean)));
      const entries = await Promise.all(bookIds.map(async (id) => {
        try {
          const book = await booksApi.getById(id);
          return [id, book.title];
        } catch {
          return [id, 'Unknown book'];
        }
      }));
      setBookTitles(Object.fromEntries(entries));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const requestReturn = async (item) => {
    setBusyId(item.id);
    try {
      await queuesApi.create({
        type: 'BOOK_RETURN',
        bookId: item.bookId,
        description: `Return request for ${bookTitles[item.bookId] || 'book'}`,
      });
      setToast({ severity: 'success', message: 'Return request submitted' });
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Something went wrong';
      setToast({ severity: 'error', message });
    } finally {
      setBusyId(null);
    }
  };

  const cancelReservation = async (reservation) => {
    setBusyId(reservation.id);
    try {
      await reservationsApi.cancel(reservation.id);
      setToast({ severity: 'success', message: 'Reservation cancelled' });
      load();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Something went wrong';
      setToast({ severity: 'error', message });
    } finally {
      setBusyId(null);
    }
  };

  const canRequestReturn = (item) => item.type === 'BOOK_BORROW' && item.status === 'APPROVED';

  if (loading) {
    return <Box display="flex" justifyContent="center" mt={8}><CircularProgress /></Box>;
  }

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} mb={3}>My Activity</Typography>

      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Typography variant="h6" fontWeight={700} mb={2}>Borrow &amp; Return Requests</Typography>
          {queueItems.length === 0 ? (
            <Typography color="text.secondary" py={2}>No requests yet.</Typography>
          ) : (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Book</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Due Date</TableCell>
                  <TableCell align="right">Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {queueItems.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell>{bookTitles[item.bookId] || '—'}</TableCell>
                    <TableCell>{item.type.replace('_', ' ')}</TableCell>
                    <TableCell><Chip size="small" label={item.status} color={STATUS_COLOR[item.status]} /></TableCell>
                    <TableCell>{item.dueDate ? new Date(item.dueDate).toLocaleDateString() : '—'}</TableCell>
                    <TableCell align="right">
                      {canRequestReturn(item) && (
                        <Button
                          size="small"
                          variant="outlined"
                          disabled={busyId === item.id}
                          onClick={() => requestReturn(item)}
                        >
                          Request Return
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="h6" fontWeight={700} mb={2}>Reservations</Typography>
          {reservationList.length === 0 ? (
            <Typography color="text.secondary" py={2}>No reservations yet.</Typography>
          ) : (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Book</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Expires</TableCell>
                  <TableCell align="right">Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {reservationList.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell>{r.bookTitle}</TableCell>
                    <TableCell><Chip size="small" label={r.status} color={STATUS_COLOR[r.status]} /></TableCell>
                    <TableCell>{r.expiresAt ? new Date(r.expiresAt).toLocaleDateString() : '—'}</TableCell>
                    <TableCell align="right">
                      {(r.status === 'ACTIVE' || r.status === 'NOTIFIED') && (
                        <Button size="small" color="error" disabled={busyId === r.id} onClick={() => cancelReservation(r)}>
                          Cancel
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast && <Alert severity={toast.severity} onClose={() => setToast(null)}>{toast.message}</Alert>}
      </Snackbar>
    </Box>
  );
}
