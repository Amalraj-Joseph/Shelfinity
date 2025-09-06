#!/usr/bin/env bash
set -euo pipefail

HOST="${HOST:-http://localhost:8080}"
REALM="${REALM:-shelfinity}"
CLIENT_ID="${CLIENT_ID:-shelfinity-frontend}"
CLIENT_SECRET="${CLIENT_SECRET:-}"        # only if your client is confidential
SCOPE="${SCOPE:-openid}"
TIMEOUT_SECS="${TIMEOUT_SECS:-180}"

token_ep="$HOST/realms/$REALM/protocol/openid-connect/token"
device_ep="$HOST/realms/$REALM/protocol/openid-connect/auth/device"

need() { command -v "$1" >/dev/null || { echo "Need $1" >&2; exit 1; }; }
need curl; need jq; need openssl

# PKCE
VERIFIER=$(LC_ALL=C tr -dc 'A-Za-z0-9-._~' </dev/urandom | head -c 64)
CHALLENGE=$(printf '%s' "$VERIFIER" | openssl dgst -sha256 -binary | openssl base64 -A | tr '+/' '-_' | tr -d '=')

# Start Device Flow
DATA=( -d "client_id=$CLIENT_ID" -d "scope=$SCOPE" -d "code_challenge=$CHALLENGE" -d "code_challenge_method=S256" )
[[ -n "$CLIENT_SECRET" ]] && DATA+=( -d "client_secret=$CLIENT_SECRET" )
RESP=$(curl -s -X POST "$device_ep" -H 'Content-Type: application/x-www-form-urlencoded' "${DATA[@]}")

err=$(jq -r '.error // empty' <<<"$RESP")
if [[ -n "$err" ]]; then
  echo "[error] $err: $(jq -r '.error_description // empty' <<<"$RESP")" >&2
  exit 1
fi

DEVICE_CODE=$(jq -r .device_code <<<"$RESP")
VERIFY_URL_COMPLETE=$(jq -r .verification_uri_complete <<<"$RESP")
USER_CODE=$(jq -r .user_code <<<"$RESP")
INTERVAL=$(jq -r '.interval // 2' <<<"$RESP")

echo "[ action ] Open this URL in your browser:"
echo "$VERIFY_URL_COMPLETE"
echo "[  hint  ] If prompted for a code, use: $USER_CODE"
echo

# Poll for token
end=$((SECONDS + TIMEOUT_SECS))
while (( SECONDS < end )); do
  TDATA=( -d 'grant_type=urn:ietf:params:oauth:grant-type:device_code'
          -d "device_code=$DEVICE_CODE"
          -d "client_id=$CLIENT_ID"
          -d "code_verifier=$VERIFIER" )
  [[ -n "$CLIENT_SECRET" ]] && TDATA+=( -d "client_secret=$CLIENT_SECRET" )

  T=$(curl -s -X POST "$token_ep" -H 'Content-Type: application/x-www-form-urlencoded' "${TDATA[@]}")
  terr=$(jq -r '.error // empty' <<<"$T")
  if [[ -z "$terr" ]]; then
    echo "[ token ]"
    jq -r .access_token <<<"$T"
    exit 0
  fi
  case "$terr" in
    authorization_pending) sleep "$INTERVAL" ;;
    slow_down) INTERVAL=$((INTERVAL+2)); sleep "$INTERVAL" ;;
    access_denied) echo "[error] access denied in browser" >&2; exit 2 ;;
    expired_token|invalid_grant) echo "[error] device code expired/invalid; re-run" >&2; exit 3 ;;
    *) echo "[error] $terr: $(jq -r '.error_description // empty' <<<"$T")" >&2; exit 4 ;;
  esac
done
echo "[error] timed out (${TIMEOUT_SECS}s) waiting for approval" >&2
exit 5
