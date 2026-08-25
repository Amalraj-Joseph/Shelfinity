/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { colors } from '../theme/tokens';

/**
 * Equal cells of one flat panel divided by 1px rules — replaces the old
 * "stat card" grid of separate floating cards. Max 4 per row, 2 on mobile.
 */
export default function FigureBlock({ items }) {
  return (
    <Box
      sx={{
        display: 'grid',
        gridTemplateColumns: { xs: 'repeat(2, 1fr)', sm: `repeat(${Math.min(items.length, 4)}, 1fr)` },
        border: `1px solid ${colors.divider}`,
        bgcolor: colors.surface,
      }}
    >
      {items.map((item, i) => (
        <Box
          key={item.label}
          sx={{
            p: { xs: 2, md: 3 },
            borderRight: {
              xs: i % 2 === 0 ? `1px solid ${colors.divider}` : 'none',
              sm: i < items.length - 1 ? `1px solid ${colors.divider}` : 'none',
            },
            borderBottom: {
              xs: i < items.length - 2 ? `1px solid ${colors.divider}` : 'none',
              sm: 'none',
            },
          }}
        >
          <Typography sx={{ fontSize: { xs: 28, md: 38 }, fontWeight: 800, fontVariantNumeric: 'tabular-nums', lineHeight: 1 }}>
            {item.value}
          </Typography>
          <Typography
            sx={{
              fontSize: 11,
              fontWeight: 600,
              letterSpacing: '0.08em',
              textTransform: 'uppercase',
              color: colors.textMuted,
              mt: 0.75,
            }}
          >
            {item.label}
          </Typography>
          {item.delta && (
            <Typography sx={{ fontSize: 13, color: colors.textMuted, mt: 0.5 }}>
              {item.delta}
            </Typography>
          )}
        </Box>
      ))}
    </Box>
  );
}
