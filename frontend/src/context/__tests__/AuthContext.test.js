/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthProvider, useAuth } from '../AuthContext';
import { ApiError, auth, users } from '../../api/client';

jest.mock('../../api/client', () => {
  const actual = jest.requireActual('../../api/client');
  return {
    ...actual,
    auth: { login: jest.fn(), me: jest.fn(), validate: jest.fn() },
    users: { create: jest.fn() },
    setAuthToken: jest.fn(),
  };
});

function TestConsumer() {
  const { isAuthenticated, currentUser, login } = useAuth();
  const [caughtError, setCaughtError] = React.useState('');
  const handleClick = async () => {
    try {
      await login('john.doe', 'john123');
    } catch (err) {
      setCaughtError(err.message);
    }
  };
  return (
    <div>
      <div data-testid="status">{isAuthenticated ? 'authed' : 'anon'}</div>
      <div data-testid="user">{currentUser?.name || ''}</div>
      <div data-testid="error">{caughtError}</div>
      <button onClick={handleClick}>Login</button>
    </div>
  );
}

// A minimal, unsigned-but-well-formed JWT payload for decodeJwtPayload().
function fakeJwt(claims) {
  const header = btoa(JSON.stringify({ alg: 'none' }));
  const payload = btoa(JSON.stringify(claims));
  return `${header}.${payload}.`;
}

describe('AuthContext', () => {
  beforeEach(() => {
    sessionStorage.clear();
    jest.clearAllMocks();
  });

  test('successful login populates currentUser', async () => {
    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ access_token: fakeJwt({ sub: 'kc-1', email: 'john@x.com' }) }),
    });
    auth.login.mockResolvedValue({ id: '1', name: 'John Doe', role: 'USER', active: true });

    render(<AuthProvider><TestConsumer /></AuthProvider>);
    await userEvent.click(screen.getByText('Login'));

    await waitFor(() => expect(screen.getByTestId('status')).toHaveTextContent('authed'));
    expect(screen.getByTestId('user')).toHaveTextContent('John Doe');
  });

  test('auto-syncs a profile when /auth/login 404s (first login after Keycloak registration)', async () => {
    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ access_token: fakeJwt({ sub: 'kc-new', email: 'new@x.com', name: 'New User' }) }),
    });
    auth.login
      .mockRejectedValueOnce(new ApiError(404, 'User not found', null))
      .mockResolvedValueOnce({ id: '2', name: 'New User', role: 'USER', active: false });
    users.create.mockResolvedValue({});

    render(<AuthProvider><TestConsumer /></AuthProvider>);
    await userEvent.click(screen.getByText('Login'));

    await waitFor(() => expect(users.create).toHaveBeenCalledWith(
      expect.objectContaining({ keycloakId: 'kc-new', email: 'new@x.com' }),
    ));
    await waitFor(() => expect(screen.getByTestId('status')).toHaveTextContent('authed'));
  });

  test('invalid credentials surfaces an error and does not authenticate', async () => {
    global.fetch = jest.fn().mockResolvedValue({ ok: false });

    render(<AuthProvider><TestConsumer /></AuthProvider>);
    await userEvent.click(screen.getByText('Login'));

    await waitFor(() => expect(screen.getByTestId('status')).toHaveTextContent('anon'));
  });
});
