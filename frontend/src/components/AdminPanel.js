/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState, useEffect } from 'react';
import './AdminPanel.css';

const LoadingSpinner = () => (
  <div className="loading-spinner">
    <div className="spinner"></div>
    <p>Loading admin data...</p>
  </div>
);

const ErrorMessage = ({ message, onRetry }) => (
  <div className="error-message">
    <p>{message}</p>
    <button onClick={onRetry} className="btn-retry">Try Again</button>
  </div>
);

const AdminPanel = ({ authToken }) => {
  const [activeTab, setActiveTab] = useState('overview');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [overviewData, setOverviewData] = useState({
    totalBooks: 0,
    totalUsers: 0,
    pendingRequests: 0,
    overdueBooks: 0
  });
  const [requests, setRequests] = useState([]);
  const [users, setUsers] = useState([]);
  const [processing, setProcessing] = useState(new Set());

  const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:9080/shelfinity-backend/app';

  const fetchAdminData = async () => {
    try {
      setLoading(true);
      setError(null);

      if (!authToken) {
        throw new Error('Authentication required');
      }

      // Fetch overview data
      const [booksResponse, usersResponse, queuesResponse] = await Promise.all([
        fetch(`${API_BASE_URL}/books`, {
          headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
          }
        }),
        fetch(`${API_BASE_URL}/users`, {
          headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
          }
        }),
        fetch(`${API_BASE_URL}/queues`, {
          headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
          }
        })
      ]);

      if (!booksResponse.ok || !usersResponse.ok || !queuesResponse.ok) {
        throw new Error('Failed to fetch admin data');
      }

      const [booksData, usersData, queuesData] = await Promise.all([
        booksResponse.json(),
        usersResponse.json(),
        queuesResponse.json()
      ]);

      // Calculate overview stats
      const totalBooks = booksData.length;
      const totalUsers = usersData.length;
      const pendingRequests = queuesData.filter(q => q.status === 'PENDING').length;
      const overdueBooks = queuesData.filter(q => q.status === 'OVERDUE').length;

      setOverviewData({
        totalBooks,
        totalUsers,
        pendingRequests,
        overdueBooks
      });

      setRequests(queuesData);
      setUsers(usersData);
    } catch (err) {
      setError(err.message);
      console.error('Error fetching admin data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (authToken) {
      fetchAdminData();
    }
  }, [authToken]);

  const handleRequestAction = async (requestId, action) => {
    if (processing.has(requestId)) return;

    try {
      setProcessing(prev => new Set(prev).add(requestId));

      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('Authentication required');
      }

      const newStatus = action === 'approve' ? 'APPROVED' : 'REJECTED';
      
      const response = await fetch(`${API_BASE_URL}/queues/${requestId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          status: newStatus
        })
      });

      if (!response.ok) {
        throw new Error(`Failed to ${action} request`);
      }

      // Update local state
      setRequests(prev => 
        prev.map(req => 
          req.id === requestId 
            ? { ...req, status: newStatus }
            : req
        )
      );

      // Refresh overview data
      fetchAdminData();
      
      alert(`Request ${action}d successfully!`);
    } catch (err) {
      alert(err.message);
      console.error(`Error ${action}ing request:`, err);
    } finally {
      setProcessing(prev => {
        const newSet = new Set(prev);
        newSet.delete(requestId);
        return newSet;
      });
    }
  };

  const handleUserAction = async (userId, action) => {
    if (processing.has(userId)) return;

    try {
      setProcessing(prev => new Set(prev).add(userId));

      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('Authentication required');
      }

      if (action === 'delete') {
        const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error('Failed to delete user');
        }

        // Remove user from local state
        setUsers(prev => prev.filter(user => user.id !== userId));
        alert('User deleted successfully!');
      } else if (action === 'edit') {
        // TODO: Implement user editing modal
        alert('User editing feature coming soon!');
      }
    } catch (err) {
      alert(err.message);
      console.error(`Error ${action}ing user:`, err);
    } finally {
      setProcessing(prev => {
        const newSet = new Set(prev);
        newSet.delete(userId);
        return newSet;
      });
    }
  };

  const handleRetry = () => {
    fetchAdminData();
  };

  const renderOverview = () => (
    <div className="admin-overview">
      <div className="stats-grid">
        <div className="stat-card">
          <h3>{overviewData.totalBooks.toLocaleString()}</h3>
          <p>Total Books</p>
        </div>
        <div className="stat-card">
          <h3>{overviewData.totalUsers.toLocaleString()}</h3>
          <p>Total Users</p>
        </div>
        <div className="stat-card">
          <h3>{overviewData.pendingRequests}</h3>
          <p>Pending Requests</p>
        </div>
        <div className="stat-card">
          <h3>{overviewData.overdueBooks}</h3>
          <p>Overdue Books</p>
        </div>
      </div>
    </div>
  );

  const renderRequests = () => (
    <div className="admin-requests">
      <div className="table-container">
        <table className="admin-table">
          <thead>
            <tr>
              <th>User</th>
              <th>Book</th>
              <th>Type</th>
              <th>Status</th>
              <th>Date</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {requests.map(request => (
              <tr key={request.id}>
                <td>{request.userName || 'Unknown User'}</td>
                <td>{request.bookTitle || 'Unknown Book'}</td>
                <td>{request.type || 'BORROW'}</td>
                <td>
                  <span className={`status ${request.status?.toLowerCase()}`}>
                    {request.status || 'PENDING'}
                  </span>
                </td>
                <td>{request.createdAt ? new Date(request.createdAt).toLocaleDateString() : 'N/A'}</td>
                <td>
                  <div className="action-buttons">
                    {request.status === 'PENDING' && (
                      <>
                        <button 
                          className="btn-approve"
                          onClick={() => handleRequestAction(request.id, 'approve')}
                          disabled={processing.has(request.id)}
                        >
                          {processing.has(request.id) ? 'Processing...' : 'Approve'}
                        </button>
                        <button 
                          className="btn-reject"
                          onClick={() => handleRequestAction(request.id, 'reject')}
                          disabled={processing.has(request.id)}
                        >
                          {processing.has(request.id) ? 'Processing...' : 'Reject'}
                        </button>
                      </>
                    )}
                    {request.status !== 'PENDING' && (
                      <span className="status-complete">Processed</span>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );

  const renderUsers = () => (
    <div className="admin-users">
      <div className="table-container">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map(user => (
              <tr key={user.id}>
                <td>{user.name || 'N/A'}</td>
                <td>{user.email || 'N/A'}</td>
                <td>
                  <span className={`role ${user.role?.toLowerCase()}`}>
                    {user.role || 'USER'}
                  </span>
                </td>
                <td>
                  <span className={`status ${user.status?.toLowerCase() || 'active'}`}>
                    {user.status || 'ACTIVE'}
                  </span>
                </td>
                <td>
                  <div className="action-buttons">
                    <button 
                      className="btn-edit"
                      onClick={() => handleUserAction(user.id, 'edit')}
                      disabled={processing.has(user.id)}
                    >
                      Edit
                    </button>
                    <button 
                      className="btn-delete"
                      onClick={() => {
                        if (window.confirm(`Are you sure you want to delete user ${user.name || user.email}?`)) {
                          handleUserAction(user.id, 'delete');
                        }
                      }}
                      disabled={processing.has(user.id)}
                    >
                      {processing.has(user.id) ? 'Processing...' : 'Delete'}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );

  if (loading) {
    return (
      <div className="admin-panel">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="admin-panel">
        <ErrorMessage message={error} onRetry={handleRetry} />
      </div>
    );
  }

  return (
    <div className="admin-panel">
      <div className="admin-header">
        <h1>Admin Panel</h1>
        <p>Manage your library system</p>
      </div>

      <div className="admin-tabs">
        <button
          className={`tab ${activeTab === 'overview' ? 'active' : ''}`}
          onClick={() => setActiveTab('overview')}
        >
          Overview
        </button>
        <button
          className={`tab ${activeTab === 'requests' ? 'active' : ''}`}
          onClick={() => setActiveTab('requests')}
        >
          Requests ({overviewData.pendingRequests})
        </button>
        <button
          className={`tab ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          Users ({overviewData.totalUsers})
        </button>
      </div>

      <div className="admin-content">
        {activeTab === 'overview' && renderOverview()}
        {activeTab === 'requests' && renderRequests()}
        {activeTab === 'users' && renderUsers()}
      </div>
    </div>
  );
};

export default AdminPanel;
