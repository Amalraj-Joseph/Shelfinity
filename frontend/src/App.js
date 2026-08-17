/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { RequireAdmin, RequireAuth } from './components/ProtectedRoute';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import BooksPage from './pages/BooksPage';
import MyActivityPage from './pages/MyActivityPage';
import AdminRequestsPage from './pages/admin/AdminRequestsPage';
import AdminUsersPage from './pages/admin/AdminUsersPage';
import AdminBooksPage from './pages/admin/AdminBooksPage';
import AdminReservationsPage from './pages/admin/AdminReservationsPage';
import AdminOverduePage from './pages/admin/AdminOverduePage';
import AdminEmailConfigPage from './pages/admin/AdminEmailConfigPage';
import AdminReportsPage from './pages/admin/AdminReportsPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route element={<RequireAuth />}>
            <Route element={<Layout />}>
              <Route path="/" element={<DashboardPage />} />
              <Route path="/books" element={<BooksPage />} />
              <Route path="/my-activity" element={<MyActivityPage />} />

              <Route element={<RequireAdmin />}>
                <Route path="/admin/requests" element={<AdminRequestsPage />} />
                <Route path="/admin/users" element={<AdminUsersPage />} />
                <Route path="/admin/books" element={<AdminBooksPage />} />
                <Route path="/admin/reservations" element={<AdminReservationsPage />} />
                <Route path="/admin/overdue" element={<AdminOverduePage />} />
                <Route path="/admin/email-config" element={<AdminEmailConfigPage />} />
                <Route path="/admin/reports" element={<AdminReportsPage />} />
              </Route>
            </Route>
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
