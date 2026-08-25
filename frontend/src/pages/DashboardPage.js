/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import Skeleton from '@mui/material/Skeleton';
import { books as booksApi, queues as queuesApi, reservations as reservationsApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import PageHeader from '../components/PageHeader';
import FigureBlock from '../components/FigureBlock';
import StatusTag from '../components/StatusTag';
import EmptyState from '../components/EmptyState';
import { colors } from '../theme/tokens';
import { queueStatusInfo, reservationStatusInfo, loanStatusInfo } from '../statusVocabulary';

function greeting() {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

function decapitalize(text) {
  return text.charAt(0).toLowerCase() + text.slice(1);
}

async function buildAnswerLine({ myQueue, myReservations }) {
  const booksOut = myQueue.filter((q) => q.type === 'BOOK_BORROW' && q.status === 'APPROVED' && q.dueDate);
  const pendingRequests = myQueue.filter((q) => q.status === 'PENDING').length;
  const holdsWaiting = myReservations.filter((r) => r.status === 'ACTIVE' || r.status === 'NOTIFIED').length;

  if (booksOut.length > 0) {
    const soonest = booksOut.reduce((a, b) => (new Date(a.dueDate) < new Date(b.dueDate) ? a : b));
    let title = 'A book';
    try {
      const book = await booksApi.getById(soonest.bookId);
      title = book.title;
    } catch {
      // book may have since been removed — fall back to the generic noun
    }
    const dueLabel = decapitalize(loanStatusInfo(soonest.dueDate).label);
    return `You have ${booksOut.length} ${booksOut.length === 1 ? 'book' : 'books'} out. ${title} is ${dueLabel}.`;
  }

  if (pendingRequests > 0 || holdsWaiting > 0) {
    const parts = [];
    if (pendingRequests > 0) parts.push(`${pendingRequests} request${pendingRequests === 1 ? '' : 's'} waiting`);
    if (holdsWaiting > 0) parts.push(`${holdsWaiting} on hold`);
    return `You have ${parts.join(' and ')}.`;
  }

  return 'Nothing out, nothing waiting. Browse the catalogue to find your next read.';
}

export default function DashboardPage() {
  const { currentUser } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ totalBooks: 0, availableBooks: 0, myRequests: 0, myReservations: 0 });
  const [recentActivity, setRecentActivity] = useState([]);
  const [answer, setAnswer] = useState('');

  useEffect(() => {
    let cancelled = false;
    async function load() {
      try {
        const [allBooks, myQueue, myReservations] = await Promise.all([
          booksApi.getAll(),
          queuesApi.getMine(),
          reservationsApi.getMine(),
        ]);
        if (cancelled) return;
        setStats({
          totalBooks: allBooks.length,
          availableBooks: allBooks.filter((b) => b.available).length,
          myRequests: myQueue.length,
          myReservations: myReservations.length,
        });
        const activity = [
          ...myQueue.map((q) => ({
            id: `queue-${q.id}`,
            primary: `${q.type.replace('_', ' ')} request`,
            secondary: q.createdAt,
            status: queueStatusInfo(q.status),
          })),
          ...myReservations.map((r) => ({
            id: `res-${r.id}`,
            primary: `Reservation for "${r.bookTitle}"`,
            secondary: r.createdAt,
            status: reservationStatusInfo(r.status),
          })),
        ]
          .sort((a, b) => new Date(b.secondary) - new Date(a.secondary))
          .slice(0, 6);
        setRecentActivity(activity);

        const line = await buildAnswerLine({ myQueue, myReservations });
        if (!cancelled) setAnswer(line);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return (
      <Box>
        <Skeleton width={280} height={48} sx={{ mb: 1 }} />
        <Skeleton width={420} height={28} sx={{ mb: 3 }} />
        <Skeleton height={2} sx={{ mb: 4 }} />
        <Skeleton height={100} sx={{ mb: 3 }} />
        <Skeleton height={200} />
      </Box>
    );
  }

  return (
    <Box>
      <PageHeader title={`${greeting()}, ${currentUser?.name?.split(' ')[0] || 'there'}`} answer={answer} />

      <Box sx={{ mb: 4 }}>
        <Typography sx={{ fontSize: 20, fontWeight: 800, mb: 2 }}>Recent activity</Typography>
        {recentActivity.length === 0 ? (
          <EmptyState
            headline="Nothing yet"
            description="Borrow or reserve a book from the catalogue and it'll show up here."
          />
        ) : (
          <List disablePadding sx={{ border: `1px solid ${colors.divider}`, bgcolor: colors.surface }}>
            {recentActivity.map((item, i) => (
              <ListItemButton
                key={item.id}
                divider={i < recentActivity.length - 1}
                sx={{ px: 2, py: 1.5 }}
              >
                <ListItemText
                  primary={item.primary}
                  secondary={new Date(item.secondary).toLocaleString()}
                  primaryTypographyProps={{ fontSize: 14, fontWeight: 600 }}
                  secondaryTypographyProps={{ fontSize: 12, color: colors.textMuted }}
                />
                <StatusTag label={item.status.label} variant={item.status.variant} />
              </ListItemButton>
            ))}
          </List>
        )}
      </Box>

      <FigureBlock
        items={[
          { value: stats.totalBooks, label: 'Total books' },
          { value: stats.availableBooks, label: 'Available now' },
          { value: stats.myRequests, label: 'My requests' },
          { value: stats.myReservations, label: 'My reservations' },
        ]}
      />
      <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
        <Typography
          component="button"
          onClick={() => navigate('/books')}
          sx={{ fontSize: 13, fontWeight: 600, color: colors.accent, border: 'none', bgcolor: 'transparent', cursor: 'pointer', p: 0 }}
        >
          Browse books →
        </Typography>
        <Typography
          component="button"
          onClick={() => navigate('/my-activity')}
          sx={{ fontSize: 13, fontWeight: 600, color: colors.accent, border: 'none', bgcolor: 'transparent', cursor: 'pointer', p: 0 }}
        >
          My shelf →
        </Typography>
      </Box>
    </Box>
  );
}
