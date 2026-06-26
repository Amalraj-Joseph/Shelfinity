# Shelfinity OpenShift - Deployment Improvements

## 🎯 Overview

This document explains the improvements made to simplify deployment and provide a complete "deploy and run" experience.

## ✨ Key Improvements

### 1. **Automatic URL Configuration in Deployment Script**

**Problem**: Previously relied on a post-install Kubernetes Job to configure URLs.

**Solution**: The deployment script now handles URL configuration directly:

```bash
./openshift/scripts/deploy.sh -e dev -g https://github.com/your-org/shelfinity.git
```

**What it does**:
1. ✅ Detects OpenShift cluster domain automatically
2. ✅ Deploys all services with Helm
3. ✅ Waits for routes to be created
4. ✅ Discovers actual route URLs
5. ✅ Updates ConfigMaps with correct URLs
6. ✅ Restarts deployments to apply configuration
7. ✅ Displays final URLs and credentials

**Benefits**:
- Faster deployment (no separate job)
- Easier to debug (all in one script)
- More reliable (direct control flow)
- Better error handling

### 2. **Database Seed Data**

**Problem**: Empty database after deployment.

**Solution**: Automatic seed data loading with 15 sample books.

**Included Books**:
1. The Great Gatsby - F. Scott Fitzgerald
2. 1984 - George Orwell
3. To Kill a Mockingbird - Harper Lee
4. Pride and Prejudice - Jane Austen
5. The Catcher in the Rye - J.D. Salinger
6. Harry Potter and the Sorcerer's Stone - J.K. Rowling
7. The Hobbit - J.R.R. Tolkien
8. Brave New World - Aldous Huxley
9. The Lord of the Rings - J.R.R. Tolkien
10. Animal Farm - George Orwell
11. The Chronicles of Narnia - C.S. Lewis
12. Moby-Dick - Herman Melville
13. War and Peace - Leo Tolstoy
14. The Odyssey - Homer
15. Jane Eyre - Charlotte Brontë

**How it works**:
- Seed data stored in [`database/configmap.yaml`](./helm/shelfinity/templates/database/configmap.yaml)
- Automatically loaded during database initialization
- Uses `ON CONFLICT DO NOTHING` for idempotency

### 3. **Keycloak Seed Users**

**Problem**: No users to test with after deployment.

**Solution**: Pre-configured realm with 3 test users.

**Included Users**:

| Username | Password | Role | Email |
|----------|----------|------|-------|
| admin | admin123 | admin | admin@shelfinity.com |
| john.doe | john123 | user | john.doe@shelfinity.com |
| jane.smith | jane123 | user | jane.smith@shelfinity.com |

**How it works**:
- Realm configuration in [`keycloak/realm-configmap.yaml`](./helm/shelfinity/templates/keycloak/realm-configmap.yaml)
- Automatically imported during Keycloak startup
- Includes proper client configurations with dynamic URLs

### 4. **CORS Pre-Configured**

**Problem**: CORS errors when frontend tries to access backend.

**Solution**: Automatic CORS configuration.

**How it works**:
1. Backend has [`CorsFilter.java`](../../backend/src/main/java/com/shelfinity/security/CorsFilter.java)
2. Deployment script sets `FRONTEND_URL` environment variable
3. Filter allows requests from frontend URL
4. All HTTP methods and headers allowed
5. Credentials supported

**CORS Headers Set**:
```
Access-Control-Allow-Origin: https://frontend-namespace.cluster.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

## 🚀 Complete Deployment Flow

### Step 1: Login to OpenShift
```bash
oc login https://your-cluster-url
```

### Step 2: Run Deployment Script
```bash
cd openshift/scripts
./deploy.sh -e dev -g https://github.com/your-org/shelfinity.git
```

### Step 3: What Happens Automatically

```
[INFO] Checking prerequisites...
[INFO] Prerequisites check passed
[INFO] Creating namespace: shelfinity-dev
[INFO] Deploying Helm chart...
[INFO] Detected cluster domain: apps.cluster.example.com
[INFO] Helm chart deployed successfully
[INFO] Configuring service URLs...
[INFO] Waiting for routes to be ready...
[INFO] Discovered Routes:
[INFO]   Frontend:  https://frontend-shelfinity-dev.apps.cluster.example.com
[INFO]   Backend:   https://backend-shelfinity-dev.apps.cluster.example.com
[INFO]   Keycloak:  https://keycloak-shelfinity-dev.apps.cluster.example.com
[INFO] Updating backend configuration...
[INFO] Updating frontend configuration...
[INFO] Restarting deployments to apply configuration...
[INFO] URL configuration complete
[INFO] Starting OpenShift builds...
[INFO] Waiting for deployments to be ready...
[INFO] Core services are ready
[INFO] Application routes:

NAME       HOST                                              TLS
frontend   frontend-shelfinity-dev.apps.cluster.example.com  edge
backend    backend-shelfinity-dev.apps.cluster.example.com   edge
keycloak   keycloak-shelfinity-dev.apps.cluster.example.com  edge

[INFO] === Deployment Complete ===
[INFO] ✓ All services deployed
[INFO] ✓ URLs configured automatically
[INFO] ✓ CORS configured
[INFO] ✓ Seed data loaded (15 books, 3 users)

[INFO] Default Credentials:
[INFO]   Admin:  admin / admin123
[INFO]   User 1: john.doe / john123
[INFO]   User 2: jane.smith / jane123

[INFO] Access your application using the routes displayed above
```

### Step 4: Access and Test

1. **Open Frontend**: `https://frontend-shelfinity-dev.apps.cluster.example.com`
2. **Login**: Use `admin` / `admin123`
3. **Browse Books**: See 15 pre-loaded books
4. **Test Features**: Borrow, return, manage users

## 📊 Comparison: Before vs After

| Feature | Before | After |
|---------|--------|-------|
| URL Configuration | Manual or post-install job | Automatic in deploy script |
| Database Data | Empty | 15 sample books |
| Keycloak Users | Manual creation | 3 pre-configured users |
| CORS Setup | Manual env var | Automatic configuration |
| Deployment Time | ~10 minutes | ~5 minutes |
| Steps to Working App | 8-10 manual steps | 1 command |
| Error Prone | Yes (many manual steps) | No (automated) |

## 🔧 Technical Details

### Deployment Script Enhancements

**New Functions**:
- `configure_urls()`: Discovers and configures all service URLs
- Auto-detects cluster domain
- Patches ConfigMaps directly
- Restarts deployments automatically

**Improved Flow**:
```bash
check_prerequisites()
create_namespace()
deploy_helm_chart()      # Now includes cluster domain detection
configure_urls()         # NEW: Direct URL configuration
start_builds()
wait_for_deployments()
display_routes()
```

### ConfigMap Updates

**Backend ConfigMap** (auto-updated):
```yaml
data:
  FRONTEND_URL: "https://frontend-namespace.cluster.com"
  OIDC_ISSUER: "https://keycloak-namespace.cluster.com/realms/shelfinity"
  DB_HOST: "postgresql"
  # ... other configs
```

**Frontend ConfigMap** (auto-updated):
```yaml
data:
  REACT_APP_KEYCLOAK_URL: "https://keycloak-namespace.cluster.com"
  REACT_APP_API_URL: "https://backend-namespace.cluster.com"
  REACT_APP_REALM: "shelfinity"
  REACT_APP_CLIENT_ID: "shelfinity-frontend"
```

### Database Seed Data

**Location**: `openshift/helm/shelfinity/templates/database/configmap.yaml`

**Loading Method**:
- Mounted as init script in PostgreSQL container
- Executed during database initialization
- Uses `ON CONFLICT DO NOTHING` for safety

### Keycloak Realm Configuration

**Location**: `openshift/helm/shelfinity/templates/keycloak/realm-configmap.yaml`

**Features**:
- Dynamic redirect URIs using Helm templates
- Pre-configured clients (frontend, backend)
- 3 test users with proper roles
- Proper OIDC/OAuth2 settings

## 🎯 Benefits

### For Developers
- ✅ **One Command Deployment**: Just run the script
- ✅ **Immediate Testing**: Login and test right away
- ✅ **No Manual Configuration**: Everything automated
- ✅ **Consistent Setup**: Same experience every time

### For DevOps
- ✅ **Faster CI/CD**: Reduced deployment time
- ✅ **Less Error-Prone**: Fewer manual steps
- ✅ **Easy Debugging**: All logs in one place
- ✅ **Reproducible**: Same result every time

### For QA/Testing
- ✅ **Ready to Test**: Immediate access to working app
- ✅ **Sample Data**: 15 books to test with
- ✅ **Multiple Users**: Test different roles
- ✅ **Full Features**: All functionality available

## 📝 Usage Examples

### Deploy to Development
```bash
./openshift/scripts/deploy.sh -e dev
```

### Deploy to Production
```bash
./openshift/scripts/deploy.sh -e prod -n shelfinity-prod
```

### Deploy with Custom Git Repo
```bash
./openshift/scripts/deploy.sh \
  -e dev \
  -g https://github.com/your-org/shelfinity.git \
  -b main
```

### Deploy to Custom Namespace
```bash
./openshift/scripts/deploy.sh \
  -n my-custom-namespace \
  -e dev
```

## 🔍 Verification

After deployment, verify everything is working:

```bash
# Check all pods are running
oc get pods -n shelfinity-dev

# Check routes
oc get routes -n shelfinity-dev

# Verify backend config
oc get configmap backend-config -n shelfinity-dev -o yaml | grep FRONTEND_URL

# Verify frontend config
oc get configmap frontend-config -n shelfinity-dev -o yaml | grep REACT_APP

# Test backend health
BACKEND_URL=$(oc get route backend -n shelfinity-dev -o jsonpath='{.spec.host}')
curl -k "https://${BACKEND_URL}/health"

# Test frontend
FRONTEND_URL=$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')
curl -k "https://${FRONTEND_URL}/"
```

## 🐛 Troubleshooting

### URLs Not Configured
```bash
# Manually run configuration
cd openshift/scripts
./deploy.sh -n shelfinity-dev -e dev
# The script will detect existing deployment and just update URLs
```

### Seed Data Not Loaded
```bash
# Check database logs
oc logs deployment/postgresql -n shelfinity-dev

# Manually load seed data
oc exec deployment/postgresql -n shelfinity-dev -- \
  psql -U shelfinity -d shelfinity -f /docker-entrypoint-initdb.d/seed-data.sql
```

### Keycloak Users Not Created
```bash
# Check Keycloak logs
oc logs deployment/keycloak -n shelfinity-dev

# Verify realm import
oc exec deployment/keycloak -n shelfinity-dev -- \
  ls -la /opt/keycloak/data/import/
```

## 📚 Related Documentation

- [Main README](./README.md) - Complete deployment guide
- [FAQ](./FAQ.md) - Frequently asked questions
- [Quick Reference](./QUICK_REFERENCE.md) - Command cheatsheet

---

**Last Updated**: 2026-03-21  
**Version**: 2.0.0 (Improved Deployment)