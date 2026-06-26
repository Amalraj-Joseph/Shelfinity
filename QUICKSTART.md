# 🚀 Shelfinity - Quick Start Guide

Get your library management system running in **5 minutes**!

## Prerequisites

- Docker Desktop installed and running
- Ports 3000, 8080, 9080, 5432 available

## Step 1: Start the System

```bash
cd Shelfinity
./scripts/dev-up.sh
```

**Wait 2-3 minutes** for all services to start.

## Step 2: Setup Keycloak Users

### Option A: Use Keycloak Admin Console

1. Open http://localhost:8080
2. Login: `admin` / `admin`
3. Select `shelfinity` realm (top-left dropdown)
4. Go to **Users** → **Add User**

**Create Admin User:**
- Username: `admin`
- Email: `admin@shelfinity.com`
- First Name: `Admin`, Last Name: `User`
- Email Verified: **ON**
- Click **Create**
- Go to **Credentials** tab → Set Password: `admin123`
- Temporary: **OFF** → Click **Set Password**
- Go to **Role Mappings** tab → Assign Role: `admin`

**Create Regular User:**
- Username: `john.doe`
- Email: `john.doe@shelfinity.com`
- First Name: `John`, Last Name: `Doe`
- Email Verified: **ON**
- Click **Create**
- Go to **Credentials** tab → Set Password: `john123`
- Temporary: **OFF** → Click **Set Password**
- Go to **Role Mappings** tab → Assign Role: `user`

### Option B: Use API (Advanced)

```bash
# Get admin token
TOKEN=$(curl -X POST "http://localhost:8080/realms/shelfinity/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin123" \
  -d "grant_type=password" \
  -d "client_id=shelfinity-frontend" \
  | jq -r '.access_token')

# Sync user to backend (get keycloakId from Keycloak admin console)
curl -X POST "http://localhost:9080/shelfinity-backend/app/users" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "keycloakId": "<keycloak-user-id>",
    "email": "admin@shelfinity.com",
    "name": "Admin User",
    "role": "ADMIN"
  }'
```

## Step 3: Access the Application

Open http://localhost:3000 and login with:
- **Admin**: `admin` / `admin123`
- **User**: `john.doe` / `john123`

## What You Can Do

### As a User:
- ✅ Browse 15 pre-loaded books
- ✅ Search and filter books
- ✅ Request to borrow books
- ✅ View your requests
- ✅ View dashboard statistics

### As an Admin:
- ✅ All user features, plus:
- ✅ Approve/reject borrow requests
- ✅ Manage users
- ✅ Add/edit/delete books
- ✅ View system statistics

## Quick Links

- **Frontend**: http://localhost:3000
- **API Docs**: http://localhost:9080/openapi/ui/
- **Keycloak**: http://localhost:8080
- **Health Check**: http://localhost:9080/health

## Stop the System

```bash
./scripts/dev-down.sh
```

## Troubleshooting

**Services not starting?**
```bash
docker-compose logs -f
```

**Port conflicts?**
```bash
# Check what's using the ports
lsof -i :3000
lsof -i :8080
lsof -i :9080
lsof -i :5432
```

**Need to reset everything?**
```bash
docker-compose down -v
docker-compose up -d --build
```

## Next Steps

- Read the full documentation: [`docs/COMPLETION_REPORT.md`](docs/COMPLETION_REPORT.md)
- Explore the API: http://localhost:9080/openapi/ui/
- Check system requirements: [`docs/api/SRS.md`](docs/api/SRS.md)

---

**Need Help?** Check [`docs/COMPLETION_REPORT.md`](docs/COMPLETION_REPORT.md) for detailed troubleshooting and configuration options.