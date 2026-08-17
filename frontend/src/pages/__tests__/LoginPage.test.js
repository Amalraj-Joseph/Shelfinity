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
import { AuthProvider, keycloakRegistrationUrl } from '../../context/AuthContext';
import { auth } from '../../api/client';

jest.mock('../../api/client', () => {
  const actual = jest.requireActual('../../api/client');
  return { ...actual, auth: { login: jest.fn(), me: jest.fn() }, setAuthToken: jest.fn() };
});

jest.mock('../../context/AuthContext', () => {
  const actual = jest.requireActual('../../context/AuthContext');
  return { ...actual, keycloakRegistrationUrl: jest.fn() };
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

  test('offers a registration link that sends the browser to Keycloak, not an in-app sign-up form', async () => {
    const registrationUrl = 'http://localhost:8080/realms/shelfinity/protocol/openid-connect/registrations'
      + '?client_id=shelfinity-frontend&response_type=code&scope=openid&redirect_uri=http%3A%2F%2Flocalhost%2Flogin'
      + '&code_challenge=abc123&code_challenge_method=S256';
    keycloakRegistrationUrl.mockResolvedValue(registrationUrl);
    delete window.location;
    window.location = { href: '' };

    renderLoginPage();
    const link = screen.getByTestId('register-link');
    await userEvent.click(link);

    await waitFor(() => expect(keycloakRegistrationUrl).toHaveBeenCalled());
    await waitFor(() => expect(window.location.href).toBe(registrationUrl));
  });
});
