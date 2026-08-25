/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Skeleton from '@mui/material/Skeleton';
import { reports as reportsApi } from '../../api/client';
import PageHeader from '../../components/PageHeader';
import FigureBlock from '../../components/FigureBlock';
import StatusTag from '../../components/StatusTag';
import EmptyState from '../../components/EmptyState';

function RankedList({ items, primary, secondary }) {
  if (!items || items.length === 0) {
    return <EmptyState headline="No data yet" description="Numbers will show up here once the library has some activity." />;
  }
  return (
    <List disablePadding>
      {items.map((item, idx) => (
        <ListItem
          key={idx} // eslint-disable-line react/no-array-index-key
          divider={idx < items.length - 1}
          sx={{ px: 0 }}
          secondaryAction={<StatusTag label={secondary(item)} variant="neutral" />}
        >
          <ListItemText
            primary={`${idx + 1}. ${primary(item)}`}
            primaryTypographyProps={{ fontSize: 14 }}
          />
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
    return (
      <Box>
        <Skeleton width={220} height={48} sx={{ mb: 1 }} />
        <Skeleton width={380} height={28} sx={{ mb: 3 }} />
        <Skeleton height={2} sx={{ mb: 4 }} />
        <Skeleton height={100} sx={{ mb: 3 }} />
        <Skeleton height={220} />
      </Box>
    );
  }

  return (
    <Box>
      <PageHeader
        title="Reports"
        answer={`${stats.totalBooks} books, ${stats.activeBorrows} active borrows, ${stats.overdueItems} overdue.`}
      />

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={4}>
          <Typography sx={{ fontSize: 20, fontWeight: 800, mb: 2 }}>Most popular books</Typography>
          <RankedList items={popularity} primary={(b) => `${b.title} — ${b.author}`} secondary={(b) => `${b.borrowCount} borrows`} />
        </Grid>
        <Grid item xs={12} md={4}>
          <Typography sx={{ fontSize: 20, fontWeight: 800, mb: 2 }}>Most active members</Typography>
          <RankedList items={activity} primary={(u) => u.userName} secondary={(u) => `${u.activityCount} actions`} />
        </Grid>
        <Grid item xs={12} md={4}>
          <Typography sx={{ fontSize: 20, fontWeight: 800, mb: 2 }}>Books by author</Typography>
          <RankedList items={authors} primary={(a) => a.author} secondary={(a) => `${a.count} books`} />
        </Grid>
      </Grid>

      <FigureBlock
        items={[
          { value: stats.totalBooks, label: 'Total books' },
          { value: stats.availableBooks, label: 'Available' },
          { value: stats.totalUsers, label: 'Total members' },
          { value: stats.activeBorrows, label: 'Active borrows' },
          { value: stats.pendingRequests, label: 'Pending requests' },
          { value: stats.overdueItems, label: 'Overdue' },
          ...(trends ? [
            { value: trends.totalBorrows, label: 'Borrows (30d)' },
            { value: trends.totalReturns, label: 'Returns (30d)' },
          ] : []),
        ]}
      />
    </Box>
  );
}
