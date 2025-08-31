/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import './Dashboard.css';

const BookIcon = ({ size = 24 }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

const SearchIcon = ({ size = 24 }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2"/>
    <path d="m21 21-4.35-4.35" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

const RequestIcon = ({ size = 24 }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M9 12l2 2 4-4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    <path d="M21 12c0 4.97-4.03 9-9 9s-9-4.03-9-9 4.03-9 9-9 9 4.03 9 9z" stroke="currentColor" strokeWidth="2"/>
  </svg>
);

const AdminIcon = ({ size = 24 }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M12 2L2 7l10 5 10-5-10-5z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    <path d="M2 17l10 5 10-5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    <path d="M2 12l10 5 10-5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

const StatsIcon = ({ size = 24 }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M3 3v18h18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    <path d="m9 9 3 3 3-3 3 3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

const ArrowRightIcon = ({ size = 24 }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M5 12h14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    <path d="m12 5 7 7-7 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

const Dashboard = ({ onViewChange }) => {
  const mockStats = {
    totalBooks: 1247,
    availableNow: 892,
    yourRequests: 3,
    recentActivity: 12
  };

  const recentActivities = [
    { id: 1, action: 'Book returned', book: 'The Great Gatsby', time: '2 hours ago' },
    { id: 2, action: 'New request', book: '1984', time: '1 day ago' },
    { id: 3, action: 'Book borrowed', book: 'To Kill a Mockingbird', time: '3 days ago' },
    { id: 4, action: 'Request approved', book: 'Pride and Prejudice', time: '1 week ago' }
  ];

  return (
    <div className="dashboard">
      {/* Welcome Section */}
      <div className="welcome-section">
        <div className="welcome-content">
          <h1>Welcome back, John!</h1>
          <p>Here's what's happening with your library today.</p>
        </div>
        <div className="user-badge">
          <span className="badge">Administrator</span>
        </div>
      </div>

      {/* Statistics Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">
            <BookIcon />
          </div>
          <div className="stat-content">
            <h3>{mockStats.totalBooks.toLocaleString()}</h3>
            <p>Total Books</p>
          </div>
        </div>
        
        <div className="stat-card">
          <div className="stat-icon">
            <StatsIcon />
          </div>
          <div className="stat-content">
            <h3>{mockStats.availableNow.toLocaleString()}</h3>
            <p>Available Now</p>
          </div>
        </div>
        
        <div className="stat-card">
          <div className="stat-icon">
            <RequestIcon />
          </div>
          <div className="stat-content">
            <h3>{mockStats.yourRequests}</h3>
            <p>Your Requests</p>
          </div>
        </div>
        
        <div className="stat-card">
          <div className="stat-icon">
            <SearchIcon />
          </div>
          <div className="stat-content">
            <h3>{mockStats.recentActivity}</h3>
            <p>Recent Activity</p>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="quick-actions">
        <h2>Quick Actions</h2>
        <div className="action-grid">
          <button 
            className="action-card"
            onClick={() => onViewChange('books')}
          >
            <div className="action-icon">
              <BookIcon />
            </div>
            <div className="action-content">
              <h3>Browse Books</h3>
              <p>Explore our collection</p>
            </div>
            <ArrowRightIcon />
          </button>
          
          <button 
            className="action-card"
            onClick={() => onViewChange('books')}
          >
            <div className="action-icon">
              <SearchIcon />
            </div>
            <div className="action-content">
              <h3>Search Library</h3>
              <p>Find specific books</p>
            </div>
            <ArrowRightIcon />
          </button>
          
          <button 
            className="action-card"
            onClick={() => onViewChange('books')}
          >
            <div className="action-icon">
              <RequestIcon />
            </div>
            <div className="action-content">
              <h3>My Requests</h3>
              <p>View your requests</p>
            </div>
            <ArrowRightIcon />
          </button>
          
          <button 
            className="action-card admin-action"
            onClick={() => onViewChange('admin')}
          >
            <div className="action-icon">
              <AdminIcon />
            </div>
            <div className="action-content">
              <h3>Admin Panel</h3>
              <p>Manage the library</p>
            </div>
            <ArrowRightIcon />
          </button>
        </div>
      </div>

      {/* Recent Activity */}
      <div className="recent-activity">
        <h2>Recent Activity</h2>
        <div className="activity-list">
          {recentActivities.map(activity => (
            <div key={activity.id} className="activity-item">
              <div className="activity-icon">
                <div className="activity-dot"></div>
              </div>
              <div className="activity-content">
                <p className="activity-text">
                  <strong>{activity.action}</strong> - {activity.book}
                </p>
                <span className="activity-time">{activity.time}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
