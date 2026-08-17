/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardActionArea from '@mui/material/CardActionArea';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Chip from '@mui/material/Chip';
import CircularProgress from '@mui/material/CircularProgress';
import MenuBookIcon from '@mui/icons-material/MenuBookOutlined';
import CheckCircleIcon from '@mui/icons-material/CheckCircleOutlined';
import AssignmentIcon from '@mui/icons-material/AssignmentOutlined';
import BookmarkIcon from '@mui/icons-material/BookmarkBorderOutlined';
import { books as booksApi, queues as queuesApi, reservations as reservationsApi } from '../api/client';
import { useAuth } from '../context/AuthContext';

const STATUS_COLOR = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'error' };

function StatCard({ icon, label, value, color, onClick }) {
  return (
    <Card>
      <CardActionArea onClick={onClick} disabled={!onClick} sx={{ p: 2.5 }}>
        <Stack direction="row" spacing={2} alignItems="center">
          <Box
            sx={{
              width: 48,
              height: 48,
              borderRadius: 2,
              bgcolor: `${color}.main`,
              opacity: 0.15,
              position: 'absolute',
            }}
          />
          <Box
            sx={{
              width: 48,
              height: 48,
              borderRadius: 2,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: `${color}.main`,
              bgcolor: (theme) => theme.palette[color].main + '22',
            }}
          >
            {icon}
          </Box>
          <Box>
            <Typography variant="h4" fontWeight={700}>{value}</Typography>
            <Typography variant="body2" color="text.secondary">{label}</Typography>
          </Box>
        </Stack>
      </CardActionArea>
    </Card>
  );
}

export default function DashboardPage() {
  const { currentUser } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ totalBooks: 0, availableBooks: 0, myRequests: 0, myReservations: 0 });
  const [recentActivity, setRecentActivity] = useState([]);

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
            secondary: new Date(q.createdAt).toLocaleString(),
            status: q.status,
          })),
          ...myReservations.map((r) => ({
            id: `res-${r.id}`,
            primary: `Reservation for "${r.bookTitle}"`,
            secondary: new Date(r.createdAt).toLocaleString(),
            status: r.status,
          })),
        ]
          .sort((a, b) => new Date(b.secondary) - new Date(a.secondary))
          .slice(0, 6);
        setRecentActivity(activity);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" mt={8}><CircularProgress /></Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} mb={0.5}>
        Welcome, {currentUser?.name?.split(' ')[0] || 'there'}
      </Typography>
      <Typography variant="body1" color="text.secondary" mb={4}>
        Here&apos;s what&apos;s happening in your library.
      </Typography>

      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard icon={<MenuBookIcon />} label="Total Books" value={stats.totalBooks} color="primary" onClick={() => navigate('/books')} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard icon={<CheckCircleIcon />} label="Available Now" value={stats.availableBooks} color="success" onClick={() => navigate('/books')} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard icon={<AssignmentIcon />} label="My Requests" value={stats.myRequests} color="secondary" onClick={() => navigate('/my-activity')} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard icon={<BookmarkIcon />} label="My Reservations" value={stats.myReservations} color="warning" onClick={() => navigate('/my-activity')} />
        </Grid>
      </Grid>

      <Card>
        <CardContent>
          <Typography variant="h6" fontWeight={700} mb={1}>Recent Activity</Typography>
          {recentActivity.length === 0 ? (
            <Typography variant="body2" color="text.secondary" py={2}>
              No activity yet — browse the catalog to borrow or reserve a book.
            </Typography>
          ) : (
            <List disablePadding>
              {recentActivity.map((item) => (
                <ListItem key={item.id} divider sx={{ px: 0 }}
                  secondaryAction={<Chip size="small" label={item.status} color={STATUS_COLOR[item.status] || 'default'} />}
                >
                  <ListItemText primary={item.primary} secondary={item.secondary} />
                </ListItem>
              ))}
            </List>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
