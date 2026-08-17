/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import { render, screen } from '@testing-library/react';
import IdentityCell from '../IdentityCell';

describe('IdentityCell', () => {
  test('shows the resolved name/title as primary text and the UUID as muted subtext', () => {
    render(<IdentityCell primary="Jane Smith" secondary="jane.smith@shelfinity.com" id="11111111-1111-1111-1111-111111111111" />);

    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    expect(screen.getByText('jane.smith@shelfinity.com')).toBeInTheDocument();
    expect(screen.queryByText('11111111-1111-1111-1111-111111111111')).not.toBeInTheDocument();
  });

  test('falls back to the raw UUID when the entity could not be resolved', () => {
    render(<IdentityCell primary={undefined} secondary={undefined} id="22222222-2222-2222-2222-222222222222" />);

    expect(screen.getByText('22222222-2222-2222-2222-222222222222')).toBeInTheDocument();
  });
});
