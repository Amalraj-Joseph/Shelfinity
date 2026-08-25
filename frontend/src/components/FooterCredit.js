/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Link from '@mui/material/Link';
import { colors } from '../theme/tokens';

const GITHUB_PROFILE_URL = 'https://github.com/Amalraj-Joseph';
export const DOCS_URL = 'https://shelfinity.amalraj.dev';

/**
 * Attribution line for the sign-in screen: this is a portfolio project, not
 * a real library's production system, so it says so plainly and links back
 * to the author's GitHub profile — same tone rules as everywhere else in
 * the app (plain, specific, no marketing voice).
 */
export default function FooterCredit({ sx }) {
  return (
    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, fontSize: 12, color: colors.textMuted, ...sx }}>
      <Typography component="span" sx={{ fontSize: 'inherit', color: 'inherit' }}>
        Portfolio project by{' '}
        <Link
          href={GITHUB_PROFILE_URL}
          target="_blank"
          rel="noopener noreferrer"
          underline="hover"
          sx={{ fontSize: 'inherit', color: colors.accent, fontWeight: 600 }}
        >
          Amalraj Joseph
        </Link>
      </Typography>
      <Link
        href={DOCS_URL}
        target="_blank"
        rel="noopener noreferrer"
        underline="hover"
        sx={{ fontSize: 'inherit', color: colors.accent, fontWeight: 600 }}
      >
        Documentation
      </Link>
    </Box>
  );
}
