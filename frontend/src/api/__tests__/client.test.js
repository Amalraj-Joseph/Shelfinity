/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import { books, setAuthToken, ApiError, health } from '../client';

function mockFetchOnce(body, { status = 200, contentType = 'application/json' } = {}) {
  global.fetch = jest.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    status,
    headers: { get: () => contentType },
    json: async () => body,
    text: async () => (typeof body === 'string' ? body : JSON.stringify(body)),
  });
}

describe('api client', () => {
  afterEach(() => {
    setAuthToken(null);
    jest.restoreAllMocks();
  });

  test('injects Authorization header when a token is set', async () => {
    mockFetchOnce([]);
    setAuthToken('my-token');

    await books.getAll();

    const [, options] = global.fetch.mock.calls[0];
    expect(options.headers.Authorization).toBe('Bearer my-token');
  });

  test('omits Authorization header when no token is set', async () => {
    mockFetchOnce([]);

    await health.check();

    const [, options] = global.fetch.mock.calls[0];
    expect(options.headers.Authorization).toBeUndefined();
  });

  test('throws ApiError with the server message on non-2xx response', async () => {
    mockFetchOnce({ error: 'Account pending approval' }, { status: 403 });

    await expect(books.getAll()).rejects.toMatchObject({
      name: 'ApiError',
      status: 403,
      message: 'Account pending approval',
    });
  });

  test('ApiError is an instance clients can check with instanceof', async () => {
    mockFetchOnce({ error: 'Not found' }, { status: 404 });

    try {
      await books.getAll();
      throw new Error('expected rejection');
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
    }
  });

  test('returns null body for 204 No Content responses', async () => {
    global.fetch = jest.fn().mockResolvedValue({ ok: true, status: 204 });

    const result = await books.remove('some-id');

    expect(result).toBeNull();
  });

  test('genre/availableOnly query params are only included when provided', async () => {
    mockFetchOnce([]);

    await books.getAll({ availableOnly: true, genre: 'Fiction' });

    const [url] = global.fetch.mock.calls[0];
    expect(url).toContain('availableOnly=true');
    expect(url).toContain('genre=Fiction');
  });

  test('bulk upload sends multipart form data without a JSON content-type header', async () => {
    mockFetchOnce({ successCount: 1, errorCount: 0 });
    const file = new File(['a,b'], 'books.csv', { type: 'text/csv' });

    await books.bulkUpload(file);

    const [, options] = global.fetch.mock.calls[0];
    expect(options.headers['Content-Type']).toBeUndefined();
    expect(options.body).toBeInstanceOf(FormData);
  });
});
