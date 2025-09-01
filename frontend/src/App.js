/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState, useEffect } from 'react';
import './App.css';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import AdminPanel from './components/AdminPanel';
import BookList from './components/BookList';
import Navbar from './components/Navbar';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);
  const [currentView, setCurrentView] = useState('dashboard');
  const [authToken, setAuthToken] = useState(null);

  const KEYCLOAK_URL = process.env.REACT_APP_KEYCLOAK_URL || 'http://localhost:8080';
  const REALM = process.env.REACT_APP_REALM || 'shelfinity';
  const CLIENT_ID = process.env.REACT_APP_CLIENT_ID || 'shelfinity-frontend';

  useEffect(() => {
    // Check if user is already authenticated
    const token = localStorage.getItem('authToken');
    if (token) {
      // Validate token with backend
      validateToken(token);
    }
  }, []);

  const validateToken = async (token) => {
    try {
      const response = await fetch(`${process.env.REACT_APP_API_URL}/auth/validate`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const userData = await response.json();
        setIsAuthenticated(true);
        setCurrentUser(userData);
        setAuthToken(token);
      } else {
        // Token is invalid, clear it
        localStorage.removeItem('authToken');
        setIsAuthenticated(false);
        setCurrentUser(null);
        setAuthToken(null);
      }
    } catch (error) {
      console.error('Token validation error:', error);
      localStorage.removeItem('authToken');
      setIsAuthenticated(false);
      setCurrentUser(null);
      setAuthToken(null);
    }
  };

  const handleLogin = async (credentials) => {
    try {
      // First, get the token from Keycloak
      const tokenResponse = await fetch(`${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          grant_type: 'password',
          client_id: CLIENT_ID,
          username: credentials.email,
          password: credentials.password,
        }),
      });

      if (!tokenResponse.ok) {
        throw new Error('Authentication failed');
      }

      const tokenData = await tokenResponse.json();
      const accessToken = tokenData.access_token;

      // Now authenticate with our backend using the Keycloak token
      const authResponse = await fetch(`${process.env.REACT_APP_API_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
      });

      if (!authResponse.ok) {
        throw new Error('Backend authentication failed');
      }

      const userData = await authResponse.json();
      
      // Store the token and user data
      localStorage.setItem('authToken', accessToken);
      setIsAuthenticated(true);
      setCurrentUser(userData);
      setAuthToken(accessToken);
      
    } catch (error) {
      console.error('Login error:', error);
      alert('Login failed: ' + error.message);
    }
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setCurrentUser(null);
    setCurrentView('dashboard');
    setAuthToken(null);
    localStorage.removeItem('authToken');
  };

  const renderContent = () => {
    switch (currentView) {
      case 'admin':
        return <AdminPanel authToken={authToken} />;
      case 'books':
        return <BookList authToken={authToken} />;
      default:
        return <Dashboard onViewChange={setCurrentView} authToken={authToken} />;
    }
  };

  if (!isAuthenticated) {
    return <Login onLogin={handleLogin} />;
  }

  return (
    <div className="App">
      <Navbar 
        user={currentUser} 
        onLogout={handleLogout}
        onViewChange={setCurrentView}
        currentView={currentView}
      />
      <main className="main-content">
        {renderContent()}
      </main>
    </div>
  );
}

export default App;
