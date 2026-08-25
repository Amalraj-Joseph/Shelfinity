/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useEffect, useRef, useState } from 'react';
import { Navigate, useSearchParams } from 'react-router-dom';
import Box from '@mui/material/Box';
import Alert from '@mui/material/Alert';
import CircularProgress from '@mui/material/CircularProgress';
import { useAuth } from '../context/AuthContext';

/**
 * Landing point for the pkce-mode Authorization Code redirect (see
 * AuthContext's AUTH_FLOW). Not reachable in the default ropc mode — nothing
 * links here — so it's safe to register unconditionally in App.js.
 */
export default function AuthCallbackPage() {
  const { handleCallback } = useAuth();
  const [searchParams] = useSearchParams();
  const [error, setError] = useState(null);
  const [done, setDone] = useState(false);
  const started = useRef(false);

  useEffect(() => {
    if (started.current) return;
    started.current = true;

    const code = searchParams.get('code');
    const state = searchParams.get('state');
    const idpError = searchParams.get('error_description') || searchParams.get('error');

    async function run() {
      if (idpError) {
        setError(idpError);
        return;
      }
      if (!code || !state) {
        setError('Sign in failed — missing authorization response');
        return;
      }
      try {
        await handleCallback(code, state);
        setDone(true);
      } catch (err) {
        setError(err.message || 'Sign in failed');
      }
    }
    run();
  }, [searchParams, handleCallback]);

  if (done) {
    return <Navigate to="/" replace />;
  }

  return (
    <Box
      minHeight="100vh"
      display="flex"
      flexDirection="column"
      alignItems="center"
      justifyContent="center"
      gap={2}
      p={2}
    >
      {error ? (
        <Alert severity="error" data-testid="callback-error">{error}</Alert>
      ) : (
        <CircularProgress />
      )}
    </Box>
  );
}
