/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Skeleton from '@mui/material/Skeleton';
import { DataGrid } from '@mui/x-data-grid';
import { overdue as overdueApi } from '../../api/client';
import IdentityCell from '../../components/IdentityCell';
import PageHeader from '../../components/PageHeader';
import FigureBlock from '../../components/FigureBlock';
import StatusTag from '../../components/StatusTag';
import { dataGridSx } from '../../components/DataTableShell';
import { loanStatusInfo } from '../../statusVocabulary';

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
      field: 'userName', headerName: 'Member', width: 220,
      renderCell: (p) => <IdentityCell primary={p.row.userName} secondary={p.row.userEmail} id={p.row.userKeycloakId} />,
    },
    {
      field: 'bookTitle', headerName: 'Book', width: 220,
      renderCell: (p) => <IdentityCell primary={p.row.bookTitle} secondary={p.row.bookIsbn} id={p.row.bookId} />,
    },
    {
      field: 'dueDate', headerName: 'Due date', width: 150,
      valueFormatter: (p) => p.value ? new Date(p.value).toLocaleDateString() : '—',
    },
    {
      field: 'status', headerName: 'Status', width: 160,
      renderCell: (p) => {
        const status = loanStatusInfo(p.row.dueDate);
        return <StatusTag label={status.label} variant={status.variant} />;
      },
    },
  ];

  if (loading) {
    return (
      <Box>
        <Skeleton width={260} height={48} sx={{ mb: 1 }} />
        <Skeleton width={380} height={28} sx={{ mb: 3 }} />
        <Skeleton height={2} sx={{ mb: 4 }} />
        <Skeleton height={100} sx={{ mb: 3 }} />
        <Skeleton height={300} />
      </Box>
    );
  }

  const answer = rows.length === 0
    ? 'Nothing is overdue. The shelves are square.'
    : `${stats.totalOverdueItems} book${stats.totalOverdueItems === 1 ? '' : 's'} overdue, ${stats.averageDaysOverdue.toFixed(1)} days late on average.`;

  return (
    <Box>
      <PageHeader title="Overdue" answer={answer} />

      {rows.length > 0 && (
        <FigureBlock
          items={[
            { value: stats.totalOverdueItems, label: 'Overdue items' },
            { value: stats.totalDaysOverdue, label: 'Total days overdue' },
            { value: stats.averageDaysOverdue.toFixed(1), label: 'Average days overdue' },
          ]}
        />
      )}

      <Box sx={{ height: 500, mt: rows.length > 0 ? 3 : 0 }}>
        <DataGrid
          rows={rows}
          columns={columns}
          disableRowSelectionOnClick
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          pageSizeOptions={[10, 25, 50]}
          sx={dataGridSx}
        />
      </Box>
    </Box>
  );
}
