/*
 * Copyright (c) 2025 Amalraj Joseph
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
import Chip from '@mui/material/Chip';
import { DataGrid } from '@mui/x-data-grid';
import { overdue as overdueApi } from '../../api/client';
import IdentityCell from '../../components/IdentityCell';

function StatBlock({ label, value }) {
  return (
    <Card>
      <CardContent>
        <Typography variant="h4" fontWeight={700}>{value}</Typography>
        <Typography variant="body2" color="text.secondary">{label}</Typography>
      </CardContent>
    </Card>
  );
}

export default function AdminOverduePage() {
  const [rows, setRows] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      const [items, statsData] = await Promise.all([overdueApi.getAll(), overdueApi.getStats()]);
      if (cancelled) return;
      setRows(items);
      setStats(statsData);
      setLoading(false);
    }
    load();
    return () => { cancelled = true; };
  }, []);

  const columns = [
    {
      field: 'userName', headerName: 'User', width: 220,
      renderCell: (p) => <IdentityCell primary={p.row.userName} secondary={p.row.userEmail} id={p.row.userKeycloakId} />,
    },
    {
      field: 'bookTitle', headerName: 'Book', width: 220,
      renderCell: (p) => <IdentityCell primary={p.row.bookTitle} secondary={p.row.bookIsbn} id={p.row.bookId} />,
    },
    {
      field: 'dueDate', headerName: 'Due Date', width: 180,
      valueFormatter: (p) => p.value ? new Date(p.value).toLocaleDateString() : '—',
    },
    {
      field: 'status', headerName: 'Status', width: 130,
      renderCell: () => <Chip size="small" label="OVERDUE" color="error" />,
    },
  ];

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} mb={0.5}>Overdue Books</Typography>
      <Typography variant="body1" color="text.secondary" mb={3}>
        Books past their due date and library-wide overdue statistics.
      </Typography>

      {stats && (
        <Grid container spacing={3} mb={3}>
          <Grid item xs={12} sm={4}>
            <StatBlock label="Overdue items" value={stats.totalOverdueItems} />
          </Grid>
          <Grid item xs={12} sm={4}>
            <StatBlock label="Total days overdue" value={stats.totalDaysOverdue} />
          </Grid>
          <Grid item xs={12} sm={4}>
            <StatBlock label="Average days overdue" value={stats.averageDaysOverdue.toFixed(1)} />
          </Grid>
        </Grid>
      )}

      <Box sx={{ height: 500, bgcolor: 'background.paper', borderRadius: 2 }}>
        <DataGrid
          rows={rows}
          columns={columns}
          loading={loading}
          disableRowSelectionOnClick
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          pageSizeOptions={[10, 25, 50]}
        />
      </Box>
    </Box>
  );
}
