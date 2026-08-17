/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';

/**
 * Renders a resolved human-readable identifier (a user's name/email or a
 * book's title/ISBN) as the primary line, with the raw UUID shown as muted
 * subtext and in a tooltip for anyone who needs the exact ID. Falls back to
 * the bare UUID if the entity behind it couldn't be resolved (e.g. deleted).
 */
export default function IdentityCell({ primary, secondary, id }) {
  const label = primary || id || '—';
  const sub = primary ? (secondary || id) : (secondary || null);
  return (
    <Tooltip title={id || ''} placement="top" arrow>
      <Box sx={{ lineHeight: 1.2, py: 0.5, overflow: 'hidden' }}>
        <Typography variant="body2" noWrap>{label}</Typography>
        {sub && (
          <Typography variant="caption" color="text.secondary" noWrap sx={{ display: 'block' }}>
            {sub}
          </Typography>
        )}
      </Box>
    </Tooltip>
  );
}
