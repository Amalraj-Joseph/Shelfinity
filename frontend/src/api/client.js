/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */

// Single source of truth for the backend base URL — replaces four duplicated
// API_BASE_URL constants that used to live in App.js/Dashboard.js/AdminPanel.js/
// BookList.js. Defaults to the same-origin "/api" the CRA dev-server proxy and
// production nginx both route to a real backend.
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || '/api';

export class ApiError extends Error {
  constructor(status, message, body) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

let authToken = null;

/** Called by AuthContext whenever the current token changes (or clears). */
export function setAuthToken(token) {
  authToken = token;
}

async function request(path, { method = 'GET', body, headers, isFormData = false } = {}) {
  const finalHeaders = { Accept: 'application/json', ...headers };
  if (!isFormData && body !== undefined) {
    finalHeaders['Content-Type'] = 'application/json';
  }
  if (authToken) {
    finalHeaders.Authorization = `Bearer ${authToken}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: finalHeaders,
    body: body === undefined ? undefined : isFormData ? body : JSON.stringify(body),
  });

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type') || '';
  const payload = contentType.includes('application/json')
    ? await response.json().catch(() => null)
    : await response.text().catch(() => null);

  if (!response.ok) {
    const message = (payload && payload.error) || `Request failed with status ${response.status}`;
    throw new ApiError(response.status, message, payload);
  }

  return payload;
}

const qs = (params = {}) => {
  const entries = Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '');
  if (entries.length === 0) return '';
  return `?${new URLSearchParams(entries).toString()}`;
};

export const health = {
  check: () => request('/health'),
};

export const auth = {
  login: () => request('/auth/login', { method: 'POST' }),
  validate: () => request('/auth/validate'),
  me: () => request('/auth/me'),
};

export const users = {
  create: (data) => request('/users', { method: 'POST', body: data }),
  getAll: () => request('/users'),
  getById: (id) => request(`/users/${id}`),
  getMe: () => request('/users/me'),
  update: (id, data) => request(`/users/${id}`, { method: 'PUT', body: data }),
  remove: (id) => request(`/users/${id}`, { method: 'DELETE' }),
};

export const books = {
  getAll: (params) => request(`/books${qs(params)}`),
  getAvailable: () => request('/books/available'),
  search: (q) => request(`/books/search${qs({ q })}`),
  getById: (id) => request(`/books/${id}`),
  create: (data) => request('/books', { method: 'POST', body: data }),
  update: (id, data) => request(`/books/${id}`, { method: 'PUT', body: data }),
  remove: (id) => request(`/books/${id}`, { method: 'DELETE' }),
  bulkUpload: (file) => {
    const form = new FormData();
    form.append('file', file);
    return request('/books/bulk-upload', { method: 'POST', body: form, isFormData: true });
  },
  bulkUploadTemplateUrl: () => `${API_BASE_URL}/books/bulk-upload/template`,
};

export const queues = {
  create: (data) => request('/queues', { method: 'POST', body: data }),
  getAll: (params) => request(`/queues${qs(params)}`),
  getById: (id) => request(`/queues/${id}`),
  getMine: () => request('/queues/my'),
  updateStatus: (id, data) => request(`/queues/${id}/status`, { method: 'PATCH', body: data }),
  remove: (id) => request(`/queues/${id}`, { method: 'DELETE' }),
};

export const reservations = {
  create: (data) => request('/reservations', { method: 'POST', body: data }),
  getAll: () => request('/reservations'),
  getMine: () => request('/reservations/my'),
  cancel: (id) => request(`/reservations/${id}`, { method: 'DELETE' }),
  fulfill: (id) => request(`/reservations/${id}/fulfill`, { method: 'POST' }),
};

export const overdue = {
  getAll: () => request('/overdue'),
  getMine: () => request('/overdue/my'),
  getStats: () => request('/overdue/stats'),
};

export const emailConfig = {
  save: (data) => request('/email/config', { method: 'POST', body: data }),
  getAll: () => request('/email/config'),
  getActive: () => request('/email/config/active'),
  update: (id, data) => request(`/email/config/${id}`, { method: 'PUT', body: data }),
  remove: (id) => request(`/email/config/${id}`, { method: 'DELETE' }),
  activate: (id) => request(`/email/config/${id}/activate`, { method: 'POST' }),
  test: (to) => request('/email/config/test', { method: 'POST', body: { to } }),
};

export const reports = {
  bookPopularity: (limit) => request(`/reports/book-popularity${qs({ limit })}`),
  borrowingTrends: (days) => request(`/reports/borrowing-trends${qs({ days })}`),
  userActivity: (limit) => request(`/reports/user-activity${qs({ limit })}`),
  statistics: () => request('/reports/statistics'),
  authorDistribution: () => request('/reports/author-distribution'),
};
