/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState } from 'react';
import './AdminPanel.css';

const AdminPanel = () => {
  const [activeTab, setActiveTab] = useState('overview');

  const mockData = {
    overview: {
      totalBooks: 1247,
      totalUsers: 342,
      pendingRequests: 23,
      overdueBooks: 8
    },
    requests: [
      { id: 1, user: 'John Doe', book: 'The Great Gatsby', status: 'pending', date: '2025-08-30' },
      { id: 2, user: 'Jane Smith', book: '1984', status: 'approved', date: '2025-08-29' },
      { id: 3, user: 'Bob Johnson', book: 'To Kill a Mockingbird', status: 'pending', date: '2025-08-28' }
    ],
    users: [
      { id: 1, name: 'John Doe', email: 'john@example.com', role: 'user', status: 'active' },
      { id: 2, name: 'Jane Smith', email: 'jane@example.com', role: 'admin', status: 'active' },
      { id: 3, name: 'Bob Johnson', email: 'bob@example.com', role: 'user', status: 'inactive' }
    ]
  };

  const renderOverview = () => (
    <div className="admin-overview">
      <div className="stats-grid">
        <div className="stat-card">
          <h3>{mockData.overview.totalBooks}</h3>
          <p>Total Books</p>
        </div>
        <div className="stat-card">
          <h3>{mockData.overview.totalUsers}</h3>
          <p>Total Users</p>
        </div>
        <div className="stat-card">
          <h3>{mockData.overview.pendingRequests}</h3>
          <p>Pending Requests</p>
        </div>
        <div className="stat-card">
          <h3>{mockData.overview.overdueBooks}</h3>
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
              <th>Status</th>
              <th>Date</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {mockData.requests.map(request => (
              <tr key={request.id}>
                <td>{request.user}</td>
                <td>{request.book}</td>
                <td>
                  <span className={`status ${request.status}`}>
                    {request.status}
                  </span>
                </td>
                <td>{request.date}</td>
                <td>
                  <div className="action-buttons">
                    <button className="btn-approve">Approve</button>
                    <button className="btn-reject">Reject</button>
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
            {mockData.users.map(user => (
              <tr key={user.id}>
                <td>{user.name}</td>
                <td>{user.email}</td>
                <td>
                  <span className={`role ${user.role}`}>
                    {user.role}
                  </span>
                </td>
                <td>
                  <span className={`status ${user.status}`}>
                    {user.status}
                  </span>
                </td>
                <td>
                  <div className="action-buttons">
                    <button className="btn-edit">Edit</button>
                    <button className="btn-delete">Delete</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );

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
          Requests
        </button>
        <button
          className={`tab ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          Users
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
