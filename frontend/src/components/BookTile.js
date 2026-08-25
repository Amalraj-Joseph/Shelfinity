/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import { colors } from '../theme/tokens';
import StatusTag from './StatusTag';

/** "All 3 copies on the shelf" / "4 of 5 on the shelf" / "All out" — the
 * design's availability-sentence pattern, computed from the aggregate
 * copy counts the backend actually has (no per-copy or waiting-count data). */
function availabilitySentence(book) {
  if (book.availableCopies <= 0) return 'All out';
  if (book.availableCopies === book.totalCopies) {
    return book.totalCopies === 1 ? '1 copy on the shelf' : `All ${book.totalCopies} copies on the shelf`;
  }
  return `${book.availableCopies} of ${book.totalCopies} on the shelf`;
}

/**
 * A compact, text-only tile — no cover art. The design's BookTile assumes a
 * cover image per title; the backend has none at all, so a large image-shaped
 * placeholder on every tile would waste space rather than stand in for a
 * photo. This keeps the flat/zero-radius/accent visual language without
 * pretending there's artwork to frame.
 */
export default function BookTile({ book, actionLabel, onAction, busy, disabled, testId }) {
  return (
    <Box
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        border: `1px solid ${colors.divider}`,
        bgcolor: colors.surface,
        p: 2,
      }}
    >
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 1, mb: 0.5 }}>
        <Typography sx={{ fontSize: 15, fontWeight: 800, lineHeight: 1.2 }}>{book.title}</Typography>
        <StatusTag
          label={book.available ? 'On the shelf' : 'All out'}
          variant={book.available ? 'neutral' : 'outline'}
          sx={{ flexShrink: 0 }}
        />
      </Box>
      <Typography sx={{ fontSize: 12, color: colors.textMuted }}>{book.author}</Typography>
      <Typography sx={{ fontSize: 12, color: colors.textMuted, mt: 0.5, mb: 1.5 }}>
        {availabilitySentence(book)}{book.genre ? ` · ${book.genre}` : ''}
      </Typography>
      {onAction && (
        <Button
          fullWidth
          size="small"
          variant={book.available ? 'contained' : 'outlined'}
          disabled={disabled || busy}
          onClick={onAction}
          data-testid={testId}
          sx={{ mt: 'auto', justifyContent: 'flex-start' }}
        >
          {busy ? <CircularProgress size={16} sx={{ color: 'inherit' }} /> : actionLabel}
        </Button>
      )}
    </Box>
  );
}
