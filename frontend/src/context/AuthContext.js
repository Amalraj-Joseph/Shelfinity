/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { ApiError, auth, setAuthToken, users } from '../api/client';
import { generatePkceChallenge, generateState } from '../auth/pkce';

const KEYCLOAK_URL = process.env.REACT_APP_KEYCLOAK_URL || 'http://localhost:8080';
const REALM = process.env.REACT_APP_REALM || 'shelfinity';
const CLIENT_ID = process.env.REACT_APP_CLIENT_ID || 'shelfinity-frontend';

// Two mutually exclusive login flows, picked at build time:
//   'ropc' (default) — today's Keycloak-only flow: an in-page form POSTs
//     credentials straight to the token endpoint. Cheap and simple, but
//     depends on the IDP allowing secret-less password-grant requests from a
//     public client, which Keycloak does and most managed IDPs (IBM Cloud
//     App ID included) don't — App ID's ROPC flow requires a client secret,
//     which a static SPA has nowhere safe to keep.
//   'pkce' — Authorization Code + PKCE against AUTHORIZATION_ENDPOINT /
//     TOKEN_ENDPOINT, the standard secret-less flow for a public/SPA client.
//     Cloud deployment (App ID) uses this; local dev stays on 'ropc' by not
//     setting this var, so nothing about local behavior changes.
const AUTH_FLOW = process.env.REACT_APP_AUTH_FLOW || 'ropc';
// Only the authorization endpoint is needed client-side: that's a top-level
// browser redirect (a real page navigation, not a fetch), so it was never
// subject to CORS. The token and userinfo endpoints are NOT called directly
// from here — confirmed against a real App ID tenant that its token
// endpoint sends no CORS headers at all, so both calls are proxied through
// the backend's POST /auth/oidc-exchange instead (see AuthResource.java).
const AUTHORIZATION_ENDPOINT = process.env.REACT_APP_OIDC_AUTHORIZATION_ENDPOINT;

const TOKEN_STORAGE_KEY = 'shelfinity.authToken';
const PKCE_VERIFIER_KEY = 'shelfinity.pkce.verifier';
const PKCE_STATE_KEY = 'shelfinity.pkce.state';

const AuthContext = createContext(null);

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

const pkceRedirectUri = () => `${window.location.origin}/auth/callback`;

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

  // `profile` (pkce mode only) comes from the backend's oidc-exchange
  // response — see handleCallback — rather than being decoded/fetched here,
  // since App ID's token carries none of these claims itself.
  const syncProfile = useCallback(async (accessToken, profile) => {
    if (AUTH_FLOW === 'pkce') {
      if (!profile?.sub) {
        throw new Error('Received an incomplete profile');
      }
      return users.create({
        keycloakId: profile.sub,
        email: profile.email,
        name: profile.name || profile.email,
      });
    }
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

  const loadCurrentUser = useCallback(async (accessToken, profile) => {
    try {
      const user = await auth.login();
      setCurrentUser(user);
      return user;
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) {
        // First login after registration — no local profile yet. SPEC.md
        // §6.1: this creates the record as active=false plus a
        // USER_REGISTRATION queue item for admin approval.
        await syncProfile(accessToken, profile);
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

  // pkce-mode equivalent of both "Sign In" and "Sign Up": the IDP's own
  // hosted page presents both, so there's no separate registration URL to
  // build here the way keycloakRegistrationUrl() does for ropc mode.
  const loginRedirect = useCallback(async () => {
    const { codeVerifier, codeChallenge } = await generatePkceChallenge();
    const state = generateState();
    sessionStorage.setItem(PKCE_VERIFIER_KEY, codeVerifier);
    sessionStorage.setItem(PKCE_STATE_KEY, state);
    const params = new URLSearchParams({
      response_type: 'code',
      client_id: CLIENT_ID,
      redirect_uri: pkceRedirectUri(),
      scope: 'openid email profile',
      code_challenge: codeChallenge,
      code_challenge_method: 'S256',
      state,
    });
    window.location.href = `${AUTHORIZATION_ENDPOINT}?${params.toString()}`;
  }, []);

  // Called by AuthCallbackPage once the IDP redirects back with ?code&state.
  const handleCallback = useCallback(async (code, state) => {
    setError(null);
    const expectedState = sessionStorage.getItem(PKCE_STATE_KEY);
    const codeVerifier = sessionStorage.getItem(PKCE_VERIFIER_KEY);
    sessionStorage.removeItem(PKCE_STATE_KEY);
    sessionStorage.removeItem(PKCE_VERIFIER_KEY);
    if (!expectedState || state !== expectedState || !codeVerifier) {
      throw new Error('Sign in failed — the login request could not be verified');
    }
    const { accessToken, sub, email, name } = await auth.oidcExchange(code, codeVerifier, pkceRedirectUri());
    applyToken(accessToken);
    await loadCurrentUser(accessToken, { sub, email, name });
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
    authFlow: AUTH_FLOW,
    login,
    loginRedirect,
    handleCallback,
    logout,
    refreshCurrentUser,
  }), [token, currentUser, loading, error, login, loginRedirect, handleCallback, logout, refreshCurrentUser]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
