/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import {
  Header,
  HeaderContainer,
  HeaderName,
  HeaderNavigation,
  HeaderMenuItem,
  HeaderGlobalBar,
  HeaderGlobalAction,
  SkipToContent
} from '@carbon/react';
import { Logout, UserAvatar } from '@carbon/icons-react';
import './Navbar.scss';

const Navbar = ({ user, onLogout, onViewChange, currentView }) => {
  return (
    <HeaderContainer
      render={() => (
        <Header aria-label="Shelfinity">
          <SkipToContent />
          <HeaderName prefix="">
            Shelfinity
          </HeaderName>
          <HeaderNavigation aria-label="Shelfinity">
            <HeaderMenuItem
              isActive={currentView === 'dashboard'}
              onClick={() => onViewChange('dashboard')}
            >
              Dashboard
            </HeaderMenuItem>
            <HeaderMenuItem
              isActive={currentView === 'books'}
              onClick={() => onViewChange('books')}
            >
              Books
            </HeaderMenuItem>
            {user?.role === 'admin' && (
              <HeaderMenuItem
                isActive={currentView === 'admin'}
                onClick={() => onViewChange('admin')}
              >
                Admin
              </HeaderMenuItem>
            )}
          </HeaderNavigation>
          <HeaderGlobalBar>
            <div className="user-info-header">
              <UserAvatar size={20} />
              <span className="user-name">{user?.name || 'User'}</span>
              {user?.role === 'admin' && (
                <span className="user-role-badge">Admin</span>
              )}
            </div>
            <HeaderGlobalAction
              aria-label="Logout"
              tooltipAlignment="end"
              onClick={onLogout}
            >
              <Logout size={20} />
            </HeaderGlobalAction>
          </HeaderGlobalBar>
        </Header>
      )}
    />
  );
};

export default Navbar;

// Made with Bob
