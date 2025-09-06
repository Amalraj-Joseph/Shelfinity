VERIFIER=$(LC_ALL=C tr -dc 'A-Za-z0-9-._~' </dev/urandom | head -c 64)
echo "verifier(len=${#VERIFIER}): $VERIFIER"
printf '%s' "$VERIFIER" | hexdump -C | head -n1

CHALLENGE=$(printf '%s' "$VERIFIER" | openssl dgst -sha256 -binary | openssl base64 -A | tr '+/' '-_' | tr -d '=')
echo "challenge: $CHALLENGE"

RESP=$(curl -s -X POST 'http://localhost:8080/realms/shelfinity/protocol/openid-connect/auth/device' -H 'Content-Type: application/x-www-form-urlencoded' -d 'client_id=shelfinity-frontend' -d 'scope=openid' -d "code_challenge=$CHALLENGE" -d 'code_challenge_method=S256')
echo "$RESP" | jq .
USER_CODE=$(echo "$RESP" | jq -r .user_code)
VERIFY_URL=$(echo "$RESP" | jq -r .verification_uri)
VERIFY_URL_COMPLETE=$(echo "$RESP" | jq -r .verification_uri_complete)
DEVICE_CODE=$(echo "$RESP" | jq -r .device_code)
echo "User code: $USER_CODE"
echo "Verification page: $VERIFY_URL"
echo "One-click link: $VERIFY_URL_COMPLETE"
echo "Device code: ${DEVICE_CODE:0:20}..."

printf '%s\n' "$VERIFY_URL_COMPLETE"
read -r -p "Press Enter after you finish login/approval..."

TOKEN_JSON=$(curl -s -X POST 'http://localhost:8080/realms/shelfinity/protocol/openid-connect/token' -H 'Content-Type: application/x-www-form-urlencoded' -d 'grant_type=urn:ietf:params:oauth:grant-type:device_code' -d "device_code=$DEVICE_CODE" -d 'client_id=shelfinity-frontend' -d "code_verifier=$VERIFIER")
echo "$TOKEN_JSON" | jq .
ACCESS_TOKEN=$(echo "$TOKEN_JSON" | jq -r .access_token)
echo "access_token length: ${#ACCESS_TOKEN}"

echo "access_token"
echo "$ACCESS_TOKEN"

# Decode payload (base64url) -> JSON
PAYLOAD_B64URL=$(cut -d. -f2 <<<"$ACCESS_TOKEN")
PAD=$(( (4 - ${#PAYLOAD_B64URL} % 4) % 4 ))
PAYLOAD_B64=$(printf '%s' "$PAYLOAD_B64URL" | tr '_-' '/+')$(printf '=%.0s' $(seq 1 $PAD))
JSON=$(printf '%s' "$PAYLOAD_B64" | base64 -d 2>/dev/null)

echo "$JSON" | jq .

# Extract and print human times (UTC and Asia/Kolkata)
for k in iat exp nbf auth_time; do
  v=$(jq -r --arg k "$k" '.[$k] // empty' <<<"$JSON")
  [ -n "$v" ] || continue
  echo "$k: $v"
  echo -n "  $k (UTC): " && date -u -d "@$v"
  echo -n "  $k (IST):  " && TZ=Asia/Kolkata date -d "@$v"
done

