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
 * Left-aligned, never centered, never an illustration. Headline says what
 * the place is for; one sentence; one optional action.
 */
export default function EmptyState({ headline, description, action, sx }) {
  return (
    <Box sx={{ py: 4, textAlign: 'left', ...sx }}>
      <Typography sx={{ fontSize: 18, fontWeight: 800, mb: 0.75 }}>{headline}</Typography>
      {description && (
        <Typography sx={{ fontSize: 14, color: colors.textMuted, maxWidth: 480 }}>
          {description}
        </Typography>
      )}
      {action && <Box sx={{ mt: 2 }}>{action}</Box>}
    </Box>
  );
}
