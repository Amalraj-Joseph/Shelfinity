/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LoginPage from '../LoginPage';
import { AuthProvider } from '../../context/AuthContext';
import { auth } from '../../api/client';

jest.mock('../../api/client', () => {
  const actual = jest.requireActual('../../api/client');
  return { ...actual, auth: { login: jest.fn(), me: jest.fn() }, setAuthToken: jest.fn() };
});

function renderLoginPage() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <LoginPage />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    sessionStorage.clear();
    jest.clearAllMocks();
  });

  test('accepts a plain username, not just an email — Keycloak usernames are not emails', async () => {
    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ access_token: 'header.eyJzdWIiOiJrYy0xIn0=.sig' }),
    });
    auth.login.mockResolvedValue({ id: '1', name: 'Admin', role: 'ADMIN', active: true });

    renderLoginPage();
    await userEvent.type(screen.getByTestId('login-username'), 'admin');
    await userEvent.type(screen.getByTestId('login-password'), 'admin123');
    await userEvent.click(screen.getByTestId('login-submit'));

    await waitFor(() => expect(auth.login).toHaveBeenCalled());
  });

  test('shows a validation error when fields are empty', async () => {
    renderLoginPage();
    await userEvent.click(screen.getByTestId('login-submit'));

    expect(await screen.findByText(/required/i)).toBeInTheDocument();
  });

  test('shows an error message when Keycloak rejects the credentials', async () => {
    global.fetch = jest.fn().mockResolvedValue({ ok: false });

    renderLoginPage();
    await userEvent.type(screen.getByTestId('login-username'), 'admin');
    await userEvent.type(screen.getByTestId('login-password'), 'wrong');
    await userEvent.click(screen.getByTestId('login-submit'));

    expect(await screen.findByText(/invalid username or password/i)).toBeInTheDocument();
  });

  test('offers a registration link pointing at Keycloak, not an in-app sign-up form', () => {
    renderLoginPage();

    const link = screen.getByText('Register');
    expect(link.closest('a')).toHaveAttribute('href', expect.stringContaining('/protocol/openid-connect/registrations'));
  });
});
