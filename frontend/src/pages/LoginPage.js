/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Alert from '@mui/material/Alert';
import CircularProgress from '@mui/material/CircularProgress';
import Link from '@mui/material/Link';
import Stack from '@mui/material/Stack';
import AutoStoriesIcon from '@mui/icons-material/AutoStories';
import { keycloakRegistrationUrl, useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth();
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

  return (
    <Box
      minHeight="100vh"
      display="flex"
      alignItems="center"
      justifyContent="center"
      sx={{
        background: 'linear-gradient(135deg, #4338CA 0%, #6366F1 50%, #0EA5E9 100%)',
        p: 2,
      }}
    >
      <Paper elevation={0} sx={{ width: '100%', maxWidth: 420, p: 5, borderRadius: 4 }}>
        <Stack alignItems="center" spacing={1} mb={4}>
          <Box
            sx={{
              width: 56,
              height: 56,
              borderRadius: 3,
              bgcolor: 'primary.main',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <AutoStoriesIcon sx={{ color: 'white', fontSize: 30 }} />
          </Box>
          <Typography variant="h5" fontWeight={700}>Welcome back</Typography>
          <Typography variant="body2" color="text.secondary">Sign in to Shelfinity</Typography>
        </Stack>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

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
              {submitting ? <CircularProgress size={24} color="inherit" /> : 'Sign in'}
            </Button>
          </Stack>
        </Box>

        <Typography variant="body2" align="center" sx={{ mt: 3 }} color="text.secondary">
          Don&apos;t have an account?{' '}
          <Link href="#" onClick={handleRegisterClick} underline="hover" data-testid="register-link">
            Register
          </Link>
        </Typography>
      </Paper>
    </Box>
  );
}
