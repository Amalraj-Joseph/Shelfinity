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
  Tabs,
  TabList,
  Tab,
  TabPanels,
  TabPanel,
  Tile,
  DataTable,
  TableContainer,
  Table,
  TableHead,
  TableRow,
  TableHeader,
  TableBody,
  TableCell,
  Button,
  Tag,
  Loading
} from '@carbon/react';
import './AdminPanel.scss';

const LoadingSpinner = () => (
  <div className="loading-container">
    <Loading description="Loading admin data..." withOverlay={false} />
  </div>
);

const ErrorMessage = ({ message, onRetry }) => (
  <div className="error-container">
    <p>{message}</p>
    <Button onClick={onRetry} kind="tertiary">Try Again</Button>
  </div>
);

const AdminPanel = ({ authToken }) => {
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

  const API_BASE_URL = '/api';

  const fetchAdminData = async () => {
    try {
      setLoading(true);
      setError(null);

      if (!authToken) {
        throw new Error('Authentication required');
      }

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

      const token = localStorage.getItem('authToken');
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

      setRequests(prev => 
        prev.map(req => 
          req.id === requestId 
            ? { ...req, status: newStatus }
            : req
        )
      );

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

      const token = localStorage.getItem('authToken');
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

        setUsers(prev => prev.filter(user => user.id !== userId));
        alert('User deleted successfully!');
      } else if (action === 'edit') {
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

  const requestHeaders = [
    { key: 'userName', header: 'User' },
    { key: 'bookTitle', header: 'Book' },
    { key: 'type', header: 'Type' },
    { key: 'status', header: 'Status' },
    { key: 'createdAt', header: 'Date' },
    { key: 'actions', header: 'Actions' }
  ];

  const requestRows = requests.map(request => ({
    id: request.id.toString(),
    userName: request.userName || 'Unknown User',
    bookTitle: request.bookTitle || 'Unknown Book',
    type: request.type || 'BORROW',
    status: request.status || 'PENDING',
    createdAt: request.createdAt ? new Date(request.createdAt).toLocaleDateString() : 'N/A',
    requestId: request.id,
    isPending: request.status === 'PENDING'
  }));

  const userHeaders = [
    { key: 'name', header: 'Name' },
    { key: 'email', header: 'Email' },
    { key: 'role', header: 'Role' },
    { key: 'status', header: 'Status' },
    { key: 'actions', header: 'Actions' }
  ];

  const userRows = users.map(user => ({
    id: user.id.toString(),
    name: user.name || 'N/A',
    email: user.email || 'N/A',
    role: user.role || 'USER',
    status: user.status || 'ACTIVE',
    userId: user.id
  }));

  return (
    <div className="admin-panel">
      <Grid>
        <Column sm={4} md={8} lg={16}>
          <div className="admin-header">
            <h1>Admin Panel</h1>
            <p>Manage your library system</p>
          </div>
        </Column>
      </Grid>

      <Tabs>
        <TabList aria-label="Admin tabs">
          <Tab>Overview</Tab>
          <Tab>Requests ({overviewData.pendingRequests})</Tab>
          <Tab>Users ({overviewData.totalUsers})</Tab>
        </TabList>
        <TabPanels>
          <TabPanel>
            <Grid narrow className="stats-grid">
              <Column sm={4} md={4} lg={4}>
                <Tile className="stat-tile">
                  <h3>{overviewData.totalBooks.toLocaleString()}</h3>
                  <p>Total Books</p>
                </Tile>
              </Column>
              <Column sm={4} md={4} lg={4}>
                <Tile className="stat-tile">
                  <h3>{overviewData.totalUsers.toLocaleString()}</h3>
                  <p>Total Users</p>
                </Tile>
              </Column>
              <Column sm={4} md={4} lg={4}>
                <Tile className="stat-tile">
                  <h3>{overviewData.pendingRequests}</h3>
                  <p>Pending Requests</p>
                </Tile>
              </Column>
              <Column sm={4} md={4} lg={4}>
                <Tile className="stat-tile">
                  <h3>{overviewData.overdueBooks}</h3>
                  <p>Overdue Books</p>
                </Tile>
              </Column>
            </Grid>
          </TabPanel>
          <TabPanel>
            <Grid>
              <Column sm={4} md={8} lg={16}>
                <DataTable rows={requestRows} headers={requestHeaders}>
                  {({
                    rows,
                    headers,
                    getHeaderProps,
                    getRowProps,
                    getTableProps,
                    getTableContainerProps
                  }) => (
                    <TableContainer {...getTableContainerProps()}>
                      <Table {...getTableProps()}>
                        <TableHead>
                          <TableRow>
                            {headers.map((header) => (
                              <TableHeader key={header.key} {...getHeaderProps({ header })}>
                                {header.header}
                              </TableHeader>
                            ))}
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {rows.map((row) => (
                            <TableRow key={row.id} {...getRowProps({ row })}>
                              {row.cells.map((cell) => {
                                if (cell.info.header === 'status') {
                                  const statusType = 
                                    cell.value === 'PENDING' ? 'blue' :
                                    cell.value === 'APPROVED' ? 'green' :
                                    cell.value === 'REJECTED' ? 'red' : 'gray';
                                  return (
                                    <TableCell key={cell.id}>
                                      <Tag type={statusType} size="sm">
                                        {cell.value}
                                      </Tag>
                                    </TableCell>
                                  );
                                }
                                if (cell.info.header === 'actions') {
                                  const requestData = requests.find(r => r.id.toString() === row.id);
                                  return (
                                    <TableCell key={cell.id}>
                                      {requestData?.status === 'PENDING' ? (
                                        <div className="action-buttons">
                                          <Button
                                            size="sm"
                                            kind="primary"
                                            onClick={() => handleRequestAction(requestData.id, 'approve')}
                                            disabled={processing.has(requestData.id)}
                                          >
                                            {processing.has(requestData.id) ? 'Processing...' : 'Approve'}
                                          </Button>
                                          <Button
                                            size="sm"
                                            kind="danger"
                                            onClick={() => handleRequestAction(requestData.id, 'reject')}
                                            disabled={processing.has(requestData.id)}
                                          >
                                            {processing.has(requestData.id) ? 'Processing...' : 'Reject'}
                                          </Button>
                                        </div>
                                      ) : (
                                        <span className="status-complete">Processed</span>
                                      )}
                                    </TableCell>
                                  );
                                }
                                return <TableCell key={cell.id}>{cell.value}</TableCell>;
                              })}
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  )}
                </DataTable>
              </Column>
            </Grid>
          </TabPanel>
          <TabPanel>
            <Grid>
              <Column sm={4} md={8} lg={16}>
                <DataTable rows={userRows} headers={userHeaders}>
                  {({
                    rows,
                    headers,
                    getHeaderProps,
                    getRowProps,
                    getTableProps,
                    getTableContainerProps
                  }) => (
                    <TableContainer {...getTableContainerProps()}>
                      <Table {...getTableProps()}>
                        <TableHead>
                          <TableRow>
                            {headers.map((header) => (
                              <TableHeader key={header.key} {...getHeaderProps({ header })}>
                                {header.header}
                              </TableHeader>
                            ))}
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {rows.map((row) => (
                            <TableRow key={row.id} {...getRowProps({ row })}>
                              {row.cells.map((cell) => {
                                if (cell.info.header === 'role') {
                                  return (
                                    <TableCell key={cell.id}>
                                      <Tag
                                        type={cell.value === 'admin' ? 'purple' : 'gray'}
                                        size="sm"
                                      >
                                        {cell.value}
                                      </Tag>
                                    </TableCell>
                                  );
                                }
                                if (cell.info.header === 'status') {
                                  return (
                                    <TableCell key={cell.id}>
                                      <Tag
                                        type={cell.value === 'ACTIVE' ? 'green' : 'red'}
                                        size="sm"
                                      >
                                        {cell.value}
                                      </Tag>
                                    </TableCell>
                                  );
                                }
                                if (cell.info.header === 'actions') {
                                  const userData = users.find(u => u.id.toString() === row.id);
                                  return (
                                    <TableCell key={cell.id}>
                                      <div className="action-buttons">
                                        <Button
                                          size="sm"
                                          kind="tertiary"
                                          onClick={() => handleUserAction(userData.id, 'edit')}
                                          disabled={processing.has(userData.id)}
                                        >
                                          Edit
                                        </Button>
                                        <Button
                                          size="sm"
                                          kind="danger--tertiary"
                                          onClick={() => {
                                            if (window.confirm(`Are you sure you want to delete user ${userData.name || userData.email}?`)) {
                                              handleUserAction(userData.id, 'delete');
                                            }
                                          }}
                                          disabled={processing.has(userData.id)}
                                        >
                                          {processing.has(userData.id) ? 'Processing...' : 'Delete'}
                                        </Button>
                                      </div>
                                    </TableCell>
                                  );
                                }
                                return <TableCell key={cell.id}>{cell.value}</TableCell>;
                              })}
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  )}
                </DataTable>
              </Column>
            </Grid>
          </TabPanel>
        </TabPanels>
      </Tabs>
    </div>
  );
};

export default AdminPanel;

// Made with Bob
