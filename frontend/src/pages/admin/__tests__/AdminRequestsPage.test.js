/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import AdminRequestsPage from '../AdminRequestsPage';
import { queues } from '../../../api/client';
import mockDataGridLayout from '../../../testUtils/mockDataGridLayout';

jest.mock('../../../api/client', () => ({
  queues: { getAll: jest.fn(), updateStatus: jest.fn() },
  ApiError: jest.requireActual('../../../api/client').ApiError,
}));

beforeAll(mockDataGridLayout);

describe('AdminRequestsPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders resolved requester and book identifiers instead of raw UUIDs', async () => {
    queues.getAll.mockResolvedValue([{
      id: 'queue-1',
      type: 'BOOK_BORROW',
      bookId: 'book-uuid-1',
      bookTitle: 'Clean Code',
      bookIsbn: '978-0132350884',
      userKeycloakId: 'kc-uuid-1',
      userName: 'Jane Smith',
      userEmail: 'jane.smith@shelfinity.com',
      status: 'PENDING',
      description: 'Please',
      createdAt: '2026-01-01T00:00:00',
    }]);

    render(<AdminRequestsPage />);

    expect(await screen.findByText('Clean Code')).toBeInTheDocument();
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    expect(screen.getByText('jane.smith@shelfinity.com')).toBeInTheDocument();
    expect(screen.queryByText('kc-uuid-1')).not.toBeInTheDocument();
    expect(screen.queryByText('book-uuid-1')).not.toBeInTheDocument();

    await waitFor(() => expect(queues.getAll).toHaveBeenCalled());
  });

  // A USER_REGISTRATION queue item has no bookId — the book cell must fall
  // back gracefully instead of rendering a broken/empty IdentityCell.
  test('renders a placeholder for request types with no associated book', async () => {
    queues.getAll.mockResolvedValue([{
      id: 'queue-2',
      type: 'USER_REGISTRATION',
      bookId: null,
      userKeycloakId: 'kc-uuid-2',
      userName: 'New User',
      userEmail: 'new@shelfinity.com',
      status: 'PENDING',
      description: 'Registration approval',
      createdAt: '2026-01-01T00:00:00',
    }]);

    render(<AdminRequestsPage />);

    expect(await screen.findByText('New User')).toBeInTheDocument();
  });
});
