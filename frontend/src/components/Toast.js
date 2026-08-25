/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import Snackbar from '@mui/material/Snackbar';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { colors, shadows } from '../theme/tokens';

/**
 * Bottom-left, flat surface, accent left border, one sentence. Restyles the
 * existing Snackbar+Alert confirmation pattern each page already has — same
 * single-toast trigger (open/message/severity), new look only.
 */
export default function Toast({ toast, onClose, autoHideDuration = 8000 }) {
  return (
    <Snackbar
      open={Boolean(toast)}
      autoHideDuration={autoHideDuration}
      onClose={onClose}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
    >
      {toast && (
        <Box
          role="alert"
          aria-live={toast.severity === 'error' ? 'assertive' : 'polite'}
          sx={{
            width: 380,
            maxWidth: '90vw',
            bgcolor: colors.bg,
            boxShadow: shadows.lg,
            borderLeft: `3px solid ${toast.severity === 'error' ? colors.accentRamp[700] : colors.accent}`,
            p: '14px 16px',
          }}
        >
          <Typography sx={{ fontSize: 14 }}>{toast.message}</Typography>
        </Box>
      )}
    </Snackbar>
  );
}
