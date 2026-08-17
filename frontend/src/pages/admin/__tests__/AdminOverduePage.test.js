/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import AdminOverduePage from '../AdminOverduePage';
import { overdue } from '../../../api/client';
import mockDataGridLayout from '../../../testUtils/mockDataGridLayout';

jest.mock('../../../api/client', () => ({
  overdue: { getAll: jest.fn(), getStats: jest.fn() },
}));

beforeAll(mockDataGridLayout);

describe('AdminOverduePage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    overdue.getStats.mockResolvedValue({ totalOverdueItems: 1, totalDaysOverdue: 2, averageDaysOverdue: 2 });
  });

  test('renders resolved user and book identifiers instead of raw UUIDs', async () => {
    overdue.getAll.mockResolvedValue([{
      id: 'item-1',
      bookId: 'book-uuid-1',
      bookTitle: 'Clean Code',
      bookIsbn: '978-0132350884',
      userKeycloakId: 'kc-uuid-1',
      userName: 'Jane Smith',
      userEmail: 'jane.smith@shelfinity.com',
      dueDate: '2026-01-01T00:00:00',
    }]);

    render(<AdminOverduePage />);

    expect(await screen.findByText('Clean Code')).toBeInTheDocument();
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    expect(screen.getByText('jane.smith@shelfinity.com')).toBeInTheDocument();
    expect(screen.queryByText('kc-uuid-1')).not.toBeInTheDocument();
    expect(screen.queryByText('book-uuid-1')).not.toBeInTheDocument();

    await waitFor(() => expect(overdue.getAll).toHaveBeenCalled());
  });
});
