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
import { loanStatusInfo } from '../statusVocabulary';

/**
 * 4px bar, elapsed/total of the loan period. The fill switches to accent
 * once inside 3 days or overdue — always paired with a text label so it
 * reads without color. Percentage is computed from processedAt (when the
 * borrow was approved, i.e. the loan started) and dueDate — both fields
 * QueueItem already carries, no new backend data needed.
 */
export default function DueMeter({ startDate, dueDate }) {
  const { label, variant } = loanStatusInfo(dueDate);
  const urgent = variant === 'accent' || variant === 'solid';

  let percent = 0;
  if (startDate && dueDate) {
    const start = new Date(startDate).getTime();
    const due = new Date(dueDate).getTime();
    const now = Date.now();
    const total = due - start;
    percent = total > 0 ? Math.min(100, Math.max(0, ((now - start) / total) * 100)) : 100;
  }

  return (
    <Box sx={{ minWidth: 140 }}>
      <Box sx={{ height: 4, bgcolor: colors.neutral[300], mb: 0.75 }}>
        <Box
          sx={{
            height: '100%',
            width: `${percent}%`,
            bgcolor: urgent ? colors.accent : colors.text,
          }}
        />
      </Box>
      <Typography sx={{ fontSize: 12, color: urgent ? colors.accentRamp[700] : colors.textMuted }}>
        {label}
      </Typography>
    </Box>
  );
}
