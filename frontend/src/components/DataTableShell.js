/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import Box from '@mui/material/Box';
import { colors } from '../theme/tokens';

/** Surface shell with a 1px divider border, wrapping a plain MUI <Table>. */
export const TableShell = Box;

export const tableShellSx = {
  bgcolor: colors.surface,
  border: `1px solid ${colors.divider}`,
  // Narrow viewports scroll the table horizontally rather than squeezing
  // columns or overflowing the page.
  overflowX: 'auto',
};

/** Row-height/hover treatment for plain <TableRow> body rows — 44px rows. */
export const tableRowSx = {
  height: 44,
  '&:hover': { bgcolor: 'rgba(32, 30, 29, 0.04)' },
  '& td': { py: 1 },
};

/** Same visual language, for pages still on MUI X DataGrid. */
export const dataGridSx = {
  border: `1px solid ${colors.divider}`,
  borderRadius: 0,
  bgcolor: colors.surface,
  '--DataGrid-rowBorderColor': colors.divider,
  '& .MuiDataGrid-columnHeaders': {
    borderBottom: `2px solid ${colors.divider}`,
  },
  '& .MuiDataGrid-columnHeaderTitle': {
    fontSize: 11,
    fontWeight: 600,
    letterSpacing: '0.08em',
    textTransform: 'uppercase',
    color: colors.textMuted,
  },
  '& .MuiDataGrid-cell': {
    fontSize: 13,
    borderBottom: `1px solid ${colors.divider}`,
  },
  '& .MuiDataGrid-cell:focus, & .MuiDataGrid-cell:focus-within': {
    outline: `2px solid ${colors.accent}`,
    outlineOffset: '-2px',
  },
  '& .MuiDataGrid-row:hover': { bgcolor: 'rgba(32, 30, 29, 0.04)' },
  '& .MuiDataGrid-row.Mui-selected': { bgcolor: colors.accentRamp[100] },
  '& .MuiDataGrid-row.Mui-selected:hover': { bgcolor: colors.accentRamp[200] },
  '& .MuiDataGrid-footerContainer': {
    borderTop: `2px solid ${colors.divider}`,
  },
  '& .MuiDataGrid-virtualScroller': {
    bgcolor: colors.surface,
  },
};
