/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState, useEffect } from 'react';
import { 
  Grid, 
  Column, 
  Tile, 
  ClickableTile,
  Loading,
  Button
} from '@carbon/react';
import { 
  Book, 
  ChartLine, 
  CheckmarkOutline, 
  Search,
  Settings,
  ArrowRight
} from '@carbon/icons-react';
import './Dashboard.scss';

const LoadingSpinner = () => (
  <div className="loading-container">
    <Loading description="Loading dashboard data..." withOverlay={false} />
  </div>
);

const ErrorMessage = ({ message, onRetry }) => (
  <div className="error-container">
    <p>{message}</p>
    <Button onClick={onRetry} kind="tertiary">Try Again</Button>
  </div>
);

const Dashboard = ({ onViewChange, authToken, currentUser }) => {
  const [stats, setStats] = useState({
    totalBooks: 0,
    availableNow: 0,
    yourRequests: 0,
    recentActivity: 0
  });
  const [recentActivities, setRecentActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);

  const API_BASE_URL = '/api';

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      if (!authToken) {
        throw new Error('Authentication required');
      }

      // Fetch books data
      const booksResponse = await fetch(`${API_BASE_URL}/books`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!booksResponse.ok) {
        throw new Error('Failed to fetch books data');
      }
      
      const booksData = await booksResponse.json();

      // Fetch user's queue items
      const queueResponse = await fetch(`${API_BASE_URL}/queues`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      let userRequests = 0;
      if (queueResponse.ok) {
        const queueData = await queueResponse.json();
        userRequests = queueData.length;
      }

      // Calculate stats
      const totalBooks = booksData.length;
      const availableNow = booksData.filter(book => book.available).length;

      setStats({
        totalBooks,
        availableNow,
        yourRequests: userRequests,
        recentActivity: 0
      });

      setRecentActivities([]);
      setLastUpdated(new Date());
    } catch (err) {
      setError(err.message);
      console.error('Dashboard data fetch error:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (authToken) {
      fetchDashboardData();
    }
  }, [authToken]);

  useEffect(() => {
    if (authToken) {
      const interval = setInterval(fetchDashboardData, 5 * 60 * 1000);
      return () => clearInterval(interval);
    }
  }, [authToken]);

  const handleRetry = () => {
    fetchDashboardData();
  };

  if (loading) {
    return (
      <div className="dashboard">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard">
        <ErrorMessage message={error} onRetry={handleRetry} />
      </div>
    );
  }

  return (
    <div className="dashboard">
      {/* Welcome Section */}
      <Grid>
        <Column sm={4} md={8} lg={16}>
          <div className="welcome-section">
            <div className="welcome-content">
              <h1>Welcome back, {currentUser?.name || 'User'}!</h1>
              <p>Here's what's happening with your library today.</p>
              {lastUpdated && (
                <small className="last-updated">
                  Last updated: {lastUpdated.toLocaleTimeString()}
                </small>
              )}
            </div>
            {currentUser?.role === 'admin' && (
              <div className="user-badge">
                <span className="badge">Administrator</span>
              </div>
            )}
          </div>
        </Column>
      </Grid>

      {/* Statistics Cards */}
      <Grid narrow className="stats-grid">
        <Column sm={4} md={4} lg={4}>
          <Tile className="stat-tile">
            <div className="stat-icon">
              <Book size={32} />
            </div>
            <div className="stat-content">
              <h3>{stats.totalBooks.toLocaleString()}</h3>
              <p>Total Books</p>
            </div>
          </Tile>
        </Column>
        
        <Column sm={4} md={4} lg={4}>
          <Tile className="stat-tile">
            <div className="stat-icon">
              <ChartLine size={32} />
            </div>
            <div className="stat-content">
              <h3>{stats.availableNow.toLocaleString()}</h3>
              <p>Available Now</p>
            </div>
          </Tile>
        </Column>
        
        <Column sm={4} md={4} lg={4}>
          <Tile className="stat-tile">
            <div className="stat-icon">
              <CheckmarkOutline size={32} />
            </div>
            <div className="stat-content">
              <h3>{stats.yourRequests}</h3>
              <p>Your Requests</p>
            </div>
          </Tile>
        </Column>
        
        <Column sm={4} md={4} lg={4}>
          <Tile className="stat-tile">
            <div className="stat-icon">
              <Search size={32} />
            </div>
            <div className="stat-content">
              <h3>{stats.recentActivity}</h3>
              <p>Recent Activity</p>
            </div>
          </Tile>
        </Column>
      </Grid>

      {/* Quick Actions */}
      <Grid>
        <Column sm={4} md={8} lg={16}>
          <h2 className="section-title">Quick Actions</h2>
        </Column>
      </Grid>

      <Grid narrow className="action-grid">
        <Column sm={4} md={4} lg={4}>
          <ClickableTile onClick={() => onViewChange('books')} className="action-tile">
            <div className="action-icon">
              <Book size={24} />
            </div>
            <div className="action-content">
              <h3>Browse Books</h3>
              <p>Explore our collection</p>
            </div>
            <ArrowRight size={20} className="action-arrow" />
          </ClickableTile>
        </Column>
        
        <Column sm={4} md={4} lg={4}>
          <ClickableTile onClick={() => onViewChange('books')} className="action-tile">
            <div className="action-icon">
              <Search size={24} />
            </div>
            <div className="action-content">
              <h3>Search Library</h3>
              <p>Find specific books</p>
            </div>
            <ArrowRight size={20} className="action-arrow" />
          </ClickableTile>
        </Column>
        
        <Column sm={4} md={4} lg={4}>
          <ClickableTile onClick={() => onViewChange('books')} className="action-tile">
            <div className="action-icon">
              <CheckmarkOutline size={24} />
            </div>
            <div className="action-content">
              <h3>My Requests</h3>
              <p>View your requests</p>
            </div>
            <ArrowRight size={20} className="action-arrow" />
          </ClickableTile>
        </Column>
        
        {currentUser?.role === 'admin' && (
          <Column sm={4} md={4} lg={4}>
            <ClickableTile onClick={() => onViewChange('admin')} className="action-tile admin-tile">
              <div className="action-icon">
                <Settings size={24} />
              </div>
              <div className="action-content">
                <h3>Admin Panel</h3>
                <p>Manage the library</p>
              </div>
              <ArrowRight size={20} className="action-arrow" />
            </ClickableTile>
          </Column>
        )}
      </Grid>

      {/* Recent Activity */}
      <Grid>
        <Column sm={4} md={8} lg={16}>
          <div className="recent-activity">
            <h2 className="section-title">Recent Activity</h2>
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
        </Column>
      </Grid>
    </div>
  );
};

export default Dashboard;

// Made with Bob
