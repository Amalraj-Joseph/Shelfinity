/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */

function base64UrlEncode(bytes) {
  let binary = '';
  bytes.forEach((b) => { binary += String.fromCharCode(b); });
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

/** RFC 7636 PKCE pair, shared by both the Keycloak registration-page
 * redirect (AuthContext's ropc mode) and the full Authorization Code + PKCE
 * flow (pkce mode). */
export async function generatePkceChallenge() {
  const verifierBytes = crypto.getRandomValues(new Uint8Array(32));
  const codeVerifier = base64UrlEncode(verifierBytes);
  const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(codeVerifier));
  const codeChallenge = base64UrlEncode(new Uint8Array(digest));
  return { codeVerifier, codeChallenge };
}

/** Opaque CSRF token for the `state` param — the pkce-mode flow does a real
 * code-for-token exchange (unlike the ropc-mode registration redirect, which
 * never exchanges its code), so this needs genuine CSRF protection. */
export function generateState() {
  return base64UrlEncode(crypto.getRandomValues(new Uint8Array(16)));
}
