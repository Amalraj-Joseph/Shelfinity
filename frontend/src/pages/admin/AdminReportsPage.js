/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Chip from '@mui/material/Chip';
import CircularProgress from '@mui/material/CircularProgress';
import { reports as reportsApi } from '../../api/client';

function StatBlock({ label, value }) {
  return (
    <Grid item xs={6} sm={3}>
      <Typography variant="h4" fontWeight={700}>{value}</Typography>
      <Typography variant="body2" color="text.secondary">{label}</Typography>
    </Grid>
  );
}

function RankedList({ items, primary, secondary }) {
  if (!items || items.length === 0) {
    return <Typography color="text.secondary" py={2}>No data yet.</Typography>;
  }
  return (
    <List disablePadding>
      {items.map((item, idx) => (
        <ListItem key={idx} divider={idx < items.length - 1} sx={{ px: 0 }}
          secondaryAction={<Chip size="small" label={secondary(item)} />}
        >
          <ListItemText primary={`${idx + 1}. ${primary(item)}`} />
        </ListItem>
      ))}
    </List>
  );
}

export default function AdminReportsPage() {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState(null);
  const [popularity, setPopularity] = useState([]);
  const [activity, setActivity] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [trends, setTrends] = useState(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      const [s, p, a, au, t] = await Promise.all([
        reportsApi.statistics(),
        reportsApi.bookPopularity(5),
        reportsApi.userActivity(5),
        reportsApi.authorDistribution(),
        reportsApi.borrowingTrends(30),
      ]);
      if (cancelled) return;
      setStats(s); setPopularity(p); setActivity(a); setAuthors(au.slice(0, 5)); setTrends(t);
      setLoading(false);
    }
    load();
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return <Box display="flex" justifyContent="center" mt={8}><CircularProgress /></Box>;
  }

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} mb={0.5}>Reports</Typography>
      <Typography variant="body1" color="text.secondary" mb={3}>Library-wide statistics and trends.</Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2}>
            <StatBlock label="Total Books" value={stats.totalBooks} />
            <StatBlock label="Available" value={stats.availableBooks} />
            <StatBlock label="Total Users" value={stats.totalUsers} />
            <StatBlock label="Active Borrows" value={stats.activeBorrows} />
            <StatBlock label="Pending Requests" value={stats.pendingRequests} />
            <StatBlock label="Overdue" value={stats.overdueItems} />
            {trends && <StatBlock label="Borrows (30d)" value={trends.totalBorrows} />}
            {trends && <StatBlock label="Returns (30d)" value={trends.totalReturns} />}
          </Grid>
        </CardContent>
      </Card>

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" fontWeight={700} mb={1}>Most Popular Books</Typography>
              <RankedList items={popularity} primary={(b) => `${b.title} — ${b.author}`} secondary={(b) => `${b.borrowCount} borrows`} />
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" fontWeight={700} mb={1}>Most Active Users</Typography>
              <RankedList items={activity} primary={(u) => u.userName} secondary={(u) => `${u.activityCount} actions`} />
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" fontWeight={700} mb={1}>Books by Author</Typography>
              <RankedList items={authors} primary={(a) => a.author} secondary={(a) => `${a.count} books`} />
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
