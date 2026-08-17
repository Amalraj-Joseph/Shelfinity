/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
const { test, expect } = require('@playwright/test');

// Runs against the real docker-compose-shelfinity.yml stack (Postgres +
// Keycloak + backend + frontend), using the seeded demo accounts from
// docker/seed-data.sql / docker/keycloak/realm-shelfinity.json.

test.describe('Critical path', () => {
  test('a user can sign in, browse the catalog, and submit a borrow request', async ({ page }) => {
    await page.goto('/login');

    await page.getByTestId('login-username').fill('jane.smith');
    await page.getByTestId('login-password').fill('jane123');
    await page.getByTestId('login-submit').click();

    await expect(page).toHaveURL('/');
    await expect(page.getByRole('heading', { name: /welcome/i })).toBeVisible();

    await page.getByRole('link', { name: /browse books/i }).click();
    await expect(page).toHaveURL(/\/books/);

    const borrowButton = page.getByRole('button', { name: /request to borrow/i }).first();
    await expect(borrowButton).toBeVisible();
    await borrowButton.click();

    // Re-running against persistent seed data can legitimately hit the
    // backend's duplicate-pending-request guard (SPEC.md §10.3) on a second
    // pass — either outcome proves the request round-trip works end to end.
    await expect(page.getByText(/borrow request submitted|pending request of this type already exists/i)).toBeVisible();
  });

  test('an admin can sign in and see the requests queue', async ({ page }) => {
    await page.goto('/login');

    await page.getByTestId('login-username').fill('admin');
    await page.getByTestId('login-password').fill('admin123');
    await page.getByTestId('login-submit').click();

    await expect(page).toHaveURL('/');
    await expect(page.getByRole('banner').getByText('Admin')).toBeVisible();

    await page.getByRole('link', { name: 'Requests' }).click();
    await expect(page).toHaveURL(/\/admin\/requests/);
    await expect(page.getByRole('heading', { name: 'Requests' })).toBeVisible();
  });

  test('a non-admin cannot reach an admin route', async ({ page }) => {
    await page.goto('/login');
    await page.getByTestId('login-username').fill('jane.smith');
    await page.getByTestId('login-password').fill('jane123');
    await page.getByTestId('login-submit').click();
    await expect(page).toHaveURL('/');

    await page.goto('/admin/users');

    await expect(page).toHaveURL('/');
  });

  test('an unauthenticated visitor is redirected to login', async ({ page }) => {
    await page.goto('/books');

    await expect(page).toHaveURL(/\/login/);
  });
});
