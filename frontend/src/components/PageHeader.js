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
 * The mandatory, identical page header from the Modernist design: a context
 * row (kicker + actions), the 42px page title, a 22px "answer line" stating
 * the page's finding, then a 2px rule before any controls. Every
 * authenticated page uses this instead of an ad hoc Typography block.
 */
export default function PageHeader({ kicker, title, answer, actions, controls }) {
  return (
    <Box component="header" sx={{ mb: { xs: 3, md: 4 } }}>
      {(kicker || actions) && (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 1, mb: 1.5 }}>
          {kicker ? (
            <Typography
              sx={{
                fontSize: 11,
                fontWeight: 600,
                letterSpacing: '0.08em',
                textTransform: 'uppercase',
                color: colors.accentRamp[700],
              }}
            >
              {kicker}
            </Typography>
          ) : <span />}
          {actions && <Box sx={{ display: 'flex', gap: 1 }}>{actions}</Box>}
        </Box>
      )}

      <Typography variant="h1" sx={{ mb: answer ? 1 : 0, fontSize: { xs: 28, sm: 36, md: 42 } }}>
        {title}
      </Typography>

      {answer && (
        <Typography sx={{ fontSize: { xs: 18, md: 22 }, fontWeight: 800, lineHeight: 1.3 }}>
          {answer}
        </Typography>
      )}

      <Box sx={{ height: 2, bgcolor: colors.divider, mt: 2, mb: controls ? 3 : 0 }} />

      {controls}
    </Box>
  );
}

/** Wraps the urgent clause of an answer line in the accent-700 ink color. */
export function Emphasis({ children }) {
  return <Box component="span" sx={{ color: colors.accentRamp[700] }}>{children}</Box>;
}
