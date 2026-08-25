/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import Box from '@mui/material/Box';
import { colors } from '../theme/tokens';

const VARIANT_STYLES = {
  neutral: { bgcolor: colors.neutral[100], color: colors.neutral[800] },
  accent: { bgcolor: colors.accentRamp[100], color: colors.accentRamp[800] },
  outline: { bgcolor: 'transparent', color: colors.accent, border: `1px solid ${colors.accent}` },
  // The one solid-accent tag in the product — reserved for "N days overdue".
  solid: { bgcolor: colors.accent, color: colors.bg },
};

/** Renders a status word from statusVocabulary.js with its ranked variant. */
export default function StatusTag({ label, variant = 'neutral', sx }) {
  const style = VARIANT_STYLES[variant] || VARIANT_STYLES.neutral;
  return (
    <Box
      component="span"
      sx={{
        display: 'inline-flex',
        alignItems: 'center',
        fontSize: 11,
        fontWeight: 600,
        letterSpacing: '0.02em',
        padding: '3px 10px',
        whiteSpace: 'nowrap',
        ...style,
        ...sx,
      }}
    >
      {label}
    </Box>
  );
}
