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

  useEffect(() => {
    // Check if user is already authenticated
    const token = localStorage.getItem('token');
    if (token) {
      setIsAuthenticated(true);
      // TODO: Validate token with backend
      setCurrentUser({ name: 'John Doe', role: 'user' });
    }
  }, []);

  const handleLogin = (userData) => {
    setIsAuthenticated(true);
    setCurrentUser(userData);
    localStorage.setItem('token', 'dummy-token');
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setCurrentUser(null);
    setCurrentView('dashboard');
    localStorage.removeItem('token');
  };

  const renderContent = () => {
    switch (currentView) {
      case 'admin':
        return <AdminPanel />;
      case 'books':
        return <BookList />;
      default:
        return <Dashboard onViewChange={setCurrentView} />;
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
