/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import BooksPage from '../BooksPage';
import { AuthProvider } from '../../context/AuthContext';
import { books, queues, reservations } from '../../api/client';

jest.mock('../../api/client', () => ({
  books: { getAll: jest.fn() },
  queues: { create: jest.fn() },
  reservations: { create: jest.fn() },
  ApiError: jest.requireActual('../../api/client').ApiError,
}));

// AuthContext isn't exported directly by design (useAuth() is the public API),
// so tests provide auth state via a lightweight local provider mirroring its
// shape instead of reaching into context internals.
jest.mock('../../context/AuthContext', () => {
  const React = require('react');
  const Ctx = React.createContext(null);
  return {
    __esModule: true,
    AuthContext: Ctx,
    useAuth: () => React.useContext(Ctx),
    AuthProvider: ({ children, value }) => <Ctx.Provider value={value}>{children}</Ctx.Provider>,
  };
});

const AVAILABLE_BOOK = {
  id: 'book-1', title: 'Clean Code', author: 'Robert C. Martin', genre: 'Software',
  available: true, availableCopies: 2, totalCopies: 3, description: 'A book about code.',
};
const UNAVAILABLE_BOOK = {
  id: 'book-2', title: 'Dune', author: 'Frank Herbert', genre: 'Sci-Fi',
  available: false, availableCopies: 0, totalCopies: 2,
};

function renderBooksPage(authOverrides = {}) {
  return render(
    <AuthProvider value={{ isPendingApproval: false, ...authOverrides }}>
      <BooksPage />
    </AuthProvider>,
  );
}

describe('BooksPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    books.getAll.mockResolvedValue([AVAILABLE_BOOK, UNAVAILABLE_BOOK]);
  });

  test('renders both available and unavailable books with the right actions', async () => {
    renderBooksPage();

    expect(await screen.findByText('Clean Code')).toBeInTheDocument();
    expect(screen.getByText('Dune')).toBeInTheDocument();
    expect(screen.getByTestId(`borrow-${AVAILABLE_BOOK.id}`)).toBeInTheDocument();
    expect(screen.getByTestId(`reserve-${UNAVAILABLE_BOOK.id}`)).toBeInTheDocument();
  });

  test('search filters by title or author', async () => {
    renderBooksPage();
    await screen.findByText('Clean Code');

    await userEvent.type(screen.getByTestId('book-search-input'), 'dune');

    expect(screen.queryByText('Clean Code')).not.toBeInTheDocument();
    expect(screen.getByText('Dune')).toBeInTheDocument();
  });

  test('borrowing submits a BOOK_BORROW queue request', async () => {
    queues.create.mockResolvedValue({});
    renderBooksPage();
    await screen.findByText('Clean Code');

    await userEvent.click(screen.getByTestId(`borrow-${AVAILABLE_BOOK.id}`));

    await waitFor(() => expect(queues.create).toHaveBeenCalledWith(
      expect.objectContaining({ type: 'BOOK_BORROW', bookId: AVAILABLE_BOOK.id }),
    ));
  });

  test('reserving an unavailable book submits a reservation', async () => {
    reservations.create.mockResolvedValue({});
    renderBooksPage();
    await screen.findByText('Dune');

    await userEvent.click(screen.getByTestId(`reserve-${UNAVAILABLE_BOOK.id}`));

    await waitFor(() => expect(reservations.create).toHaveBeenCalledWith(
      expect.objectContaining({ bookId: UNAVAILABLE_BOOK.id }),
    ));
  });

  test('borrow/reserve actions are disabled while the account is pending approval', async () => {
    renderBooksPage({ isPendingApproval: true });
    await screen.findByText('Clean Code');

    expect(screen.getByTestId(`borrow-${AVAILABLE_BOOK.id}`)).toBeDisabled();
    expect(screen.getByTestId(`reserve-${UNAVAILABLE_BOOK.id}`)).toBeDisabled();
  });
});
