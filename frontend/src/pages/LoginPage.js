/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Alert from '@mui/material/Alert';
import CircularProgress from '@mui/material/CircularProgress';
import Link from '@mui/material/Link';
import Stack from '@mui/material/Stack';
import { BookOpen } from 'lucide-react';
import { colors } from '../theme/tokens';
import readingRoomImage from '../assets/reading-room.png';
import FooterCredit from '../components/FooterCredit';
import { keycloakRegistrationUrl, useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login, loginRedirect, isAuthenticated, authFlow } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  if (isAuthenticated) {
    const redirectTo = location.state?.from || '/';
    return <Navigate to={redirectTo} replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    if (!username || !password) {
      setError('Username/email and password are required');
      return;
    }
    setSubmitting(true);
    try {
      await login(username, password);
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message || 'Sign in failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRegisterClick = async (e) => {
    e.preventDefault();
    const url = await keycloakRegistrationUrl();
    window.location.href = url;
  };

  // pkce mode: the IDP's own hosted page handles both credential entry and
  // sign-up, so there's no in-page form here at all — just a redirect.
  const handleContinueClick = async () => {
    setError(null);
    setSubmitting(true);
    try {
      await loginRedirect();
    } catch (err) {
      setError(err.message || 'Sign in failed');
      setSubmitting(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', md: '1.1fr 1fr' },
        bgcolor: colors.bg,
      }}
    >
      {/* Poster panel — hidden on mobile per the design's mobile transform rules */}
      <Box
        sx={{
          display: { xs: 'none', md: 'flex' },
          position: 'relative',
          flexDirection: 'column',
          justifyContent: 'flex-end',
          p: 6,
          overflow: 'hidden',
          bgcolor: colors.neutral[900],
        }}
      >
        <Box
          component="img"
          src={readingRoomImage}
          alt="The main reading room, looking toward the tall windows."
          sx={{
            position: 'absolute',
            inset: 0,
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            filter: 'grayscale(1) contrast(1.08)',
          }}
        />
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            background: `linear-gradient(180deg, ${colors.neutral[900]}00 40%, ${colors.neutral[900]}e6 100%)`,
          }}
        />
        <Box sx={{ position: 'relative', color: colors.bg }}>
          <Typography
            sx={{
              fontSize: { md: 44, lg: 56 },
              fontWeight: 800,
              lineHeight: 1.05,
              letterSpacing: '-0.015em',
              mb: 2,
            }}
          >
            Your library, without limits.
          </Typography>
          <Typography sx={{ fontSize: 16, opacity: 0.85, maxWidth: 420, mb: 4 }}>
            Sign in with your library account to borrow, reserve, and keep track of what you have
            out.
          </Typography>
          <FooterCredit sx={{ color: colors.bg, opacity: 0.75 }} />
        </Box>
      </Box>

      {/* Sign-in panel */}
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          px: { xs: 3, sm: 6, md: 8 },
          py: 6,
        }}
      >
        <Box sx={{ width: '100%', maxWidth: 380, mx: { xs: 'auto', md: 0 } }}>
          <Stack direction="row" alignItems="center" spacing={1} mb={5}>
            <BookOpen size={22} strokeWidth={2.25} color={colors.accent} />
            <Typography sx={{ fontSize: 18, fontWeight: 800 }}>Shelfinity</Typography>
          </Stack>

          <Typography variant="h1" sx={{ fontSize: { xs: 32, sm: 38 }, mb: 1 }}>
            Sign in
          </Typography>
          <Typography sx={{ fontSize: 15, color: colors.textMuted, mb: 4 }}>
            {authFlow === 'pkce'
              ? "You'll finish signing in with your institution, then land back here."
              : 'Enter your library username or email and password.'}
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          {authFlow === 'pkce' ? (
            <Button
              variant="contained"
              size="large"
              fullWidth
              disabled={submitting}
              onClick={handleContinueClick}
              data-testid="login-continue"
            >
              {submitting ? <CircularProgress size={20} sx={{ color: colors.bg }} /> : 'Continue'}
            </Button>
          ) : (
            <>
              <Box component="form" onSubmit={handleSubmit} noValidate>
                <Stack spacing={2.5}>
                  <TextField
                    label="Username or email"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    fullWidth
                    autoFocus
                    inputProps={{ 'data-testid': 'login-username' }}
                  />
                  <TextField
                    label="Password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    fullWidth
                    inputProps={{ 'data-testid': 'login-password' }}
                  />
                  <Button
                    type="submit"
                    variant="contained"
                    size="large"
                    fullWidth
                    disabled={submitting}
                    data-testid="login-submit"
                  >
                    {submitting ? <CircularProgress size={20} sx={{ color: colors.bg }} /> : 'Sign in'}
                  </Button>
                </Stack>
              </Box>

              <Typography sx={{ fontSize: 13, color: colors.textMuted, mt: 3 }}>
                Don&apos;t have an account?{' '}
                <Link
                  href="#"
                  onClick={handleRegisterClick}
                  underline="hover"
                  sx={{ color: colors.accent, fontWeight: 600 }}
                  data-testid="register-link"
                >
                  Register
                </Link>
              </Typography>
            </>
          )}

          <FooterCredit sx={{ display: { xs: 'flex', md: 'none' }, mt: 6 }} />
        </Box>
      </Box>
    </Box>
  );
}
