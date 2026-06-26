/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState } from 'react';
import { 
  TextInput, 
  PasswordInput, 
  Button, 
  Grid, 
  Column,
  Form,
  Stack
} from '@carbon/react';
import { Login as LoginIcon } from '@carbon/icons-react';
import './Login.scss';

const ShelfinityLogo = ({ size = 48 }) => (
  <svg width={size} height={size} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
    <rect width="64" height="64" rx="12" fill="#1a1a1a"/>
    <path d="M16 20h32v4H16v-4zm0 8h32v4H16v-4zm0 8h24v4H16v-4z" fill="#ffffff"/>
    <circle cx="48" cy="44" r="4" fill="#4a4a4a"/>
  </svg>
);

const Login = ({ onLogin }) => {
  const [isSignUp, setIsSignUp] = useState(false);
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    confirmPassword: ''
  });
  const [errors, setErrors] = useState({});

  const handleInputChange = (e) => {
    const { id, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [id]: value
    }));
    // Clear error when user starts typing
    if (errors[id]) {
      setErrors(prev => ({
        ...prev,
        [id]: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.email) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }

    if (isSignUp) {
      if (!formData.firstName) {
        newErrors.firstName = 'First name is required';
      }
      if (!formData.lastName) {
        newErrors.lastName = 'Last name is required';
      }
      if (!formData.confirmPassword) {
        newErrors.confirmPassword = 'Please confirm your password';
      } else if (formData.password !== formData.confirmPassword) {
        newErrors.confirmPassword = 'Passwords do not match';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validateForm()) {
      onLogin({
        email: formData.email,
        password: formData.password
      });
    }
  };

  return (
    <div className="login-container">
      <Grid>
        <Column sm={4} md={6} lg={8} className="login-column">
          <div className="login-card">
            <div className="login-header">
              <ShelfinityLogo size={64} />
              <h1>Shelfinity</h1>
              <p className="login-subtitle">
                {isSignUp ? 'Create your account' : 'Welcome back'}
              </p>
            </div>

            <Form onSubmit={handleSubmit} className="login-form">
              <Stack gap={5}>
                {isSignUp && (
                  <Grid narrow>
                    <Column sm={4} md={4} lg={8}>
                      <TextInput
                        id="firstName"
                        labelText="First Name"
                        value={formData.firstName}
                        onChange={handleInputChange}
                        invalid={!!errors.firstName}
                        invalidText={errors.firstName}
                        placeholder="Enter your first name"
                      />
                    </Column>
                    <Column sm={4} md={4} lg={8}>
                      <TextInput
                        id="lastName"
                        labelText="Last Name"
                        value={formData.lastName}
                        onChange={handleInputChange}
                        invalid={!!errors.lastName}
                        invalidText={errors.lastName}
                        placeholder="Enter your last name"
                      />
                    </Column>
                  </Grid>
                )}

                <TextInput
                  id="email"
                  labelText="Email"
                  type="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  invalid={!!errors.email}
                  invalidText={errors.email}
                  placeholder="Enter your email"
                />

                <PasswordInput
                  id="password"
                  labelText="Password"
                  value={formData.password}
                  onChange={handleInputChange}
                  invalid={!!errors.password}
                  invalidText={errors.password}
                  placeholder="Enter your password"
                />

                {isSignUp && (
                  <PasswordInput
                    id="confirmPassword"
                    labelText="Confirm Password"
                    value={formData.confirmPassword}
                    onChange={handleInputChange}
                    invalid={!!errors.confirmPassword}
                    invalidText={errors.confirmPassword}
                    placeholder="Confirm your password"
                  />
                )}

                <Button 
                  type="submit" 
                  renderIcon={LoginIcon}
                  className="login-button"
                >
                  {isSignUp ? 'Create Account' : 'Sign In'}
                </Button>
              </Stack>
            </Form>

            <div className="login-footer">
              <p>
                {isSignUp ? 'Already have an account?' : "Don't have an account?"}
                <Button
                  kind="ghost"
                  size="sm"
                  onClick={() => setIsSignUp(!isSignUp)}
                  className="toggle-button"
                >
                  {isSignUp ? 'Sign In' : 'Sign Up'}
                </Button>
              </p>
            </div>
          </div>
        </Column>
      </Grid>
    </div>
  );
};

export default Login;

// Made with Bob
