/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { ApiError, auth, setAuthToken, users } from '../api/client';

const KEYCLOAK_URL = process.env.REACT_APP_KEYCLOAK_URL || 'http://localhost:8080';
const REALM = process.env.REACT_APP_REALM || 'shelfinity';
const CLIENT_ID = process.env.REACT_APP_CLIENT_ID || 'shelfinity-frontend';

const TOKEN_STORAGE_KEY = 'shelfinity.authToken';

const AuthContext = createContext(null);

function base64UrlEncode(bytes) {
  let binary = '';
  bytes.forEach((b) => { binary += String.fromCharCode(b); });
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

/** RFC 7636 PKCE pair — required because the shelfinity-frontend Keycloak
 * client has pkce.code.challenge.method=S256 set, so Keycloak rejects any
 * authorization request (registration included) that omits code_challenge. */
async function generatePkceChallenge() {
  const verifierBytes = crypto.getRandomValues(new Uint8Array(32));
  const codeVerifier = base64UrlEncode(verifierBytes);
  const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(codeVerifier));
  const codeChallenge = base64UrlEncode(new Uint8Array(digest));
  return { codeVerifier, codeChallenge };
}

/**
 * Builds the URL to Keycloak's own hosted registration page. SPEC.md §4:
 * Keycloak is the sole owner of account registration — the backend has no
 * endpoint that creates a Keycloak identity, only one that syncs a profile
 * for an already-authenticated caller (see decision #2 in the implementation
 * plan for why the old in-app "Sign Up" form was removed rather than fixed).
 *
 * Async because building a valid request requires an RFC 7636 PKCE challenge
 * (see generatePkceChallenge). The redirect_uri points back at /login rather
 * than the bare origin: the realm's shelfinity-frontend client only allows
 * "http://localhost:3000/*" (a wildcard requiring a path segment), and this
 * app never exchanges the returned `code` — the user just signs in normally
 * with their new credentials once Keycloak sends them back.
 */
export async function keycloakRegistrationUrl() {
  const { codeChallenge } = await generatePkceChallenge();
  const redirectUri = encodeURIComponent(`${window.location.origin}/login`);
  return `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/registrations` +
    `?client_id=${CLIENT_ID}&response_type=code&scope=openid&redirect_uri=${redirectUri}` +
    `&code_challenge=${codeChallenge}&code_challenge_method=S256`;
}

async function fetchKeycloakToken(username, password) {
  const response = await fetch(`${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'password',
      client_id: CLIENT_ID,
      username,
      password,
    }),
  });

  if (!response.ok) {
    throw new Error('Invalid username or password');
  }
  const data = await response.json();
  return data.access_token;
}

/** Decodes the JWT payload without verifying it — display/UX use only. */
function decodeJwtPayload(token) {
  try {
    const [, payload] = token.split('.');
    return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => sessionStorage.getItem(TOKEN_STORAGE_KEY));
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const applyToken = useCallback((newToken) => {
    setToken(newToken);
    setAuthToken(newToken);
    if (newToken) {
      sessionStorage.setItem(TOKEN_STORAGE_KEY, newToken);
    } else {
      sessionStorage.removeItem(TOKEN_STORAGE_KEY);
    }
  }, []);

  const syncProfile = useCallback(async (accessToken) => {
    const claims = decodeJwtPayload(accessToken);
    if (!claims) {
      throw new Error('Received an unreadable token');
    }
    return users.create({
      keycloakId: claims.sub,
      email: claims.email || claims.preferred_username,
      name: claims.name || claims.preferred_username,
    });
  }, []);

  const loadCurrentUser = useCallback(async (accessToken) => {
    try {
      const user = await auth.login();
      setCurrentUser(user);
      return user;
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) {
        // First login after Keycloak registration — no local profile yet.
        // SPEC.md §6.1: this creates the record as active=false plus a
        // USER_REGISTRATION queue item for admin approval.
        await syncProfile(accessToken);
        const user = await auth.login();
        setCurrentUser(user);
        return user;
      }
      throw err;
    }
  }, [syncProfile]);

  const refreshCurrentUser = useCallback(async () => {
    if (!token) return;
    const user = await auth.me();
    setCurrentUser(user);
  }, [token]);

  useEffect(() => {
    let cancelled = false;
    async function bootstrap() {
      if (!token) {
        setLoading(false);
        return;
      }
      setAuthToken(token);
      try {
        await loadCurrentUser(token);
      } catch {
        if (!cancelled) {
          applyToken(null);
          setCurrentUser(null);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    bootstrap();
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const login = useCallback(async (username, password) => {
    setError(null);
    const accessToken = await fetchKeycloakToken(username, password);
    applyToken(accessToken);
    await loadCurrentUser(accessToken);
  }, [applyToken, loadCurrentUser]);

  const logout = useCallback(() => {
    applyToken(null);
    setCurrentUser(null);
  }, [applyToken]);

  const value = useMemo(() => ({
    token,
    currentUser,
    isAuthenticated: Boolean(token && currentUser),
    isAdmin: currentUser?.role === 'ADMIN',
    isPendingApproval: Boolean(currentUser && currentUser.active === false),
    loading,
    error,
    setError,
    login,
    logout,
    refreshCurrentUser,
  }), [token, currentUser, loading, error, login, logout, refreshCurrentUser]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
