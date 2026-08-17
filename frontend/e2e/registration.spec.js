/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
const { test, expect } = require('@playwright/test');

// Regression coverage for the "register page isn't loading" bug: Keycloak's
// event log (type=REGISTER_ERROR, error=invalid_request) showed the previous
// registration link failed two independent OIDC-level checks —
//   1) redirect_uri=http://localhost:3000 (bare origin) didn't match the
//      shelfinity-frontend client's "http://localhost:3000/*" wildcard, and
//   2) the client enforces PKCE (pkce.code.challenge.method=S256) but the
//      old link never sent code_challenge/code_challenge_method.
// The only prior test asserted a static href *string* and never actually
// drove a browser to Keycloak, so it could not have caught either failure.
// This spec does — it clicks the real link and asserts Keycloak's hosted
// registration form actually renders, against the live compose stack.

test.describe('Registration flow', () => {
  test('clicking Register on the login page reaches Keycloak\'s real hosted registration form', async ({ page }) => {
    await page.goto('/login');

    await page.getByTestId('register-link').click();

    await page.waitForURL(/\/realms\/shelfinity\/protocol\/openid-connect\/registrations/);
    const url = new URL(page.url());
    expect(url.searchParams.get('code_challenge_method')).toBe('S256');
    expect(url.searchParams.get('code_challenge')).toBeTruthy();
    expect(url.searchParams.get('redirect_uri')).toContain('/login');

    // Proves the request was accepted (no invalid_request error page) and
    // Keycloak actually rendered its registration form.
    await expect(page.getByLabel(/username/i)).toBeVisible();
    await expect(page.getByLabel(/^email/i)).toBeVisible();
    await expect(page.locator('input[type="password"]').first()).toBeVisible();
  });
});
