/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState } from 'react';
import './Navbar.css';

const Navbar = ({ user, onLogout, onViewChange, currentView }) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  const handleViewChange = (view) => {
    onViewChange(view);
    setIsMenuOpen(false);
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">
          <h1 className="navbar-logo">Shelfinity</h1>
        </div>

        <div className={`navbar-menu ${isMenuOpen ? 'active' : ''}`}>
          <button
            className={`nav-item ${currentView === 'dashboard' ? 'active' : ''}`}
            onClick={() => handleViewChange('dashboard')}
          >
            Dashboard
          </button>
          
          <button
            className={`nav-item ${currentView === 'books' ? 'active' : ''}`}
            onClick={() => handleViewChange('books')}
          >
            Books
          </button>
          
          {user?.role === 'admin' && (
            <button
              className={`nav-item admin-nav ${currentView === 'admin' ? 'active' : ''}`}
              onClick={() => handleViewChange('admin')}
            >
              Admin
            </button>
          )}
        </div>

        <div className="navbar-user">
          <div className="user-info">
            <span className="user-name">{user?.name || 'User'}</span>
            {user?.role === 'admin' && (
              <span className="user-role">Admin</span>
            )}
          </div>
          
          <button className="logout-btn" onClick={onLogout}>
            Logout
          </button>
        </div>

        <button className="navbar-toggle" onClick={toggleMenu}>
          <span className="hamburger"></span>
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
