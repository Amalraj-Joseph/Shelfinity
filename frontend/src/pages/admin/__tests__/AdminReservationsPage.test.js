/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import AdminReservationsPage from '../AdminReservationsPage';
import { reservations } from '../../../api/client';
import mockDataGridLayout from '../../../testUtils/mockDataGridLayout';

jest.mock('../../../api/client', () => ({
  reservations: { getAll: jest.fn(), fulfill: jest.fn() },
  ApiError: jest.requireActual('../../../api/client').ApiError,
}));

beforeAll(mockDataGridLayout);

describe('AdminReservationsPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // SPEC-adjacent UI rule: reservation rows must display the resolved book
  // title and requester name/email, not the raw UUIDs alone.
  test('renders resolved book and user identifiers instead of raw UUIDs', async () => {
    reservations.getAll.mockResolvedValue([{
      id: 'res-1',
      bookId: 'book-uuid-1',
      bookTitle: 'Clean Code',
      bookIsbn: '978-0132350884',
      userKeycloakId: 'kc-uuid-1',
      userName: 'Jane Smith',
      userEmail: 'jane.smith@shelfinity.com',
      status: 'ACTIVE',
      expiresAt: null,
    }]);

    render(<AdminReservationsPage />);

    expect(await screen.findByText('Clean Code')).toBeInTheDocument();
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    expect(screen.getByText('jane.smith@shelfinity.com')).toBeInTheDocument();
    expect(screen.queryByText('kc-uuid-1')).not.toBeInTheDocument();
    expect(screen.queryByText('book-uuid-1')).not.toBeInTheDocument();

    await waitFor(() => expect(reservations.getAll).toHaveBeenCalled());
  });
});
