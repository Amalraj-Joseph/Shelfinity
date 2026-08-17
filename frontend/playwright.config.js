/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
const { defineConfig, devices } = require('@playwright/test');

// Runs against an already-running stack (docker-compose.yml),
// same tier-design as the backend's *ApiIT suite — no attempt to spin up
// Postgres/Keycloak/backend/frontend from within the test run itself.
module.exports = defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: 'list',
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:3000',
    trace: 'retain-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
});
