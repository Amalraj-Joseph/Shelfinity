# Deploy Shelfinity from Local Working Directory

## 📍 Current Working Directory
`/Users/amalrajjoseph/Shadow-Codex/Shelfinity`
## 🔨 Binary Builds (Local Source Upload)

This deployment uses **Binary Builds**, which means:
- ✅ No Git repository required
- ✅ Source code uploaded directly from your local machine
- ✅ Perfect for local development and testing
- ✅ Fast iterations without committing code

The deployment script automatically uploads your `./backend` and `./frontend` directories to OpenShift and builds container images from them.


## 🚀 Quick Deployment Commands

### Step 1: Login to OpenShift Cluster

```bash
# Login to your OpenShift cluster
oc login https://your-openshift-cluster-url:6443

# Verify you're logged in
oc whoami
oc cluster-info
```

### Step 2: Deploy Using the Script (Recommended)

```bash
# Navigate to your project directory (if not already there)
cd /Users/amalrajjoseph/Shadow-Codex/Shelfinity

# Make the script executable (if not already)
chmod +x openshift/scripts/deploy.sh

## ⚠️ Important Notes

### Database Storage
The development configuration uses **emptyDir** (temporary storage) for the database:
- ✅ No storage configuration required
- ✅ Works on any OpenShift cluster
- ⚠️ Data is lost when pod restarts
- 💡 Perfect for development and testing

**What this means:**
- Database works immediately without storage setup
- Seed data (15 books, 3 users) loads automatically on each restart
- Great for testing and demos
- For persistent storage, you'll need to configure a proper StorageClass


# Deploy to development environment
./openshift/scripts/deploy.sh -e dev

# The script will:
# ✓ Create namespace: shelfinity-dev
# ✓ Deploy all services (PostgreSQL, Keycloak, Backend, Frontend)
# ✓ Configure URLs automatically
# ✓ Load seed data (15 books, 3 users)
# ✓ Display access URLs and credentials
```

### Step 3: Monitor Deployment

```bash
# Watch pods starting up
oc get pods -n shelfinity-dev -w

# Check deployment status
oc get deployments -n shelfinity-dev

# View routes (URLs)
oc get routes -n shelfinity-dev
```

### Step 4: Access Your Application

```bash
# Get the frontend URL
echo "Frontend: https://$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')"

# Get the backend URL
echo "Backend: https://$(oc get route backend -n shelfinity-dev -o jsonpath='{.spec.host}')"

# Get the Keycloak URL
echo "Keycloak: https://$(oc get route keycloak -n shelfinity-dev -o jsonpath='{.spec.host}')"
```

## 🔧 Alternative: Manual Helm Deployment

If you prefer to use Helm directly:

```bash
# Navigate to project directory
cd /Users/amalrajjoseph/Shadow-Codex/Shelfinity

# Create namespace
oc new-project shelfinity-dev

# Get cluster domain
CLUSTER_DOMAIN=$(oc get ingresses.config.openshift.io cluster -o jsonpath='{.spec.domain}')
echo "Cluster domain: $CLUSTER_DOMAIN"

# Deploy with Helm
helm install shelfinity ./openshift/helm/shelfinity \
  --namespace shelfinity-dev \
  --values ./openshift/helm/shelfinity/values-dev.yaml \
  --set global.namespace=shelfinity-dev \
  --set global.environment=dev \
  --set global.domain=$CLUSTER_DOMAIN \
  --wait \
  --timeout 10m

# Wait for routes to be created
sleep 15

# Configure URLs
FRONTEND_URL="https://$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')"
BACKEND_URL="https://$(oc get route backend -n shelfinity-dev -o jsonpath='{.spec.host}')"
KEYCLOAK_URL="https://$(oc get route keycloak -n shelfinity-dev -o jsonpath='{.spec.host}')"

# Update backend config
oc patch configmap backend-config -n shelfinity-dev \
  --type merge \
  -p "{\"data\":{\"FRONTEND_URL\":\"${FRONTEND_URL}\",\"OIDC_ISSUER\":\"${KEYCLOAK_URL}/realms/shelfinity\"}}"

# Update frontend config
oc patch configmap frontend-config -n shelfinity-dev \
  --type merge \
  -p "{\"data\":{\"REACT_APP_KEYCLOAK_URL\":\"${KEYCLOAK_URL}\",\"REACT_APP_API_URL\":\"${BACKEND_URL}\"}}"

# Restart deployments
oc rollout restart deployment/backend -n shelfinity-dev
oc rollout restart deployment/frontend -n shelfinity-dev

# Wait for rollouts
oc rollout status deployment/backend -n shelfinity-dev
oc rollout status deployment/frontend -n shelfinity-dev

# Display URLs
echo ""
echo "=== Deployment Complete ==="
echo "Frontend:  $FRONTEND_URL"
echo "Backend:   $BACKEND_URL"
echo "Keycloak:  $KEYCLOAK_URL"
echo ""
echo "Default Credentials:"
echo "  Admin:  admin / admin123"
echo "  User 1: john.doe / john123"
echo "  User 2: jane.smith / jane123"
```

## 📝 Complete Step-by-Step Guide

### Prerequisites Check

```bash
# Check if oc is installed
oc version

# Check if helm is installed
helm version

# Check if you're in the right directory
pwd
# Should output: /Users/amalrajjoseph/Shadow-Codex/Shelfinity

# List OpenShift files
ls -la openshift/
```

### Deployment Process

```bash
# 1. Navigate to project
cd /Users/amalrajjoseph/Shadow-Codex/Shelfinity

# 2. Login to OpenShift
oc login https://your-cluster-url:6443
# Enter your credentials when prompted

# 3. Verify login
oc whoami
oc projects

# 4. Run deployment script
./openshift/scripts/deploy.sh -e dev

# 5. Wait for completion (about 5-7 minutes)
# The script will show progress and final URLs

# 6. Verify deployment
oc get pods -n shelfinity-dev
oc get routes -n shelfinity-dev

# 7. Test backend health
BACKEND_URL=$(oc get route backend -n shelfinity-dev -o jsonpath='{.spec.host}')
curl -k "https://${BACKEND_URL}/health"

# 8. Open frontend in browser
FRONTEND_URL=$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')
open "https://${FRONTEND_URL}"  # macOS
# or
echo "Open this URL in your browser: https://${FRONTEND_URL}"
```

## 🎯 What You'll Get

After running the deployment:

### ✅ Services Deployed
- PostgreSQL database (with 15 sample books)
- Keycloak authentication (with 3 test users)
- Backend API (Jakarta EE)
- Frontend web app (React)

### ✅ Pre-configured Data

**Books** (15 classics):
- The Great Gatsby, 1984, Pride and Prejudice, Harry Potter, etc.

**Users** (3 accounts):
- `admin` / `admin123` (Administrator)
- `john.doe` / `john123` (Regular user)
- `jane.smith` / `jane123` (Regular user)

### ✅ Automatic Configuration
- All URLs configured
- CORS enabled
- TLS/HTTPS enabled
- Health checks configured

## 🔍 Verification Commands

```bash
# Check all pods are running
oc get pods -n shelfinity-dev

# Expected output:
# NAME                          READY   STATUS    RESTARTS   AGE
# postgresql-xxx                1/1     Running   0          5m
# keycloak-xxx                  1/1     Running   0          5m
# backend-xxx                   1/1     Running   0          4m
# frontend-xxx                  1/1     Running   0          4m

# Check services
oc get svc -n shelfinity-dev

# Check routes
oc get routes -n shelfinity-dev

# Check ConfigMaps
oc get configmap backend-config -n shelfinity-dev -o yaml | grep FRONTEND_URL
oc get configmap frontend-config -n shelfinity-dev -o yaml | grep REACT_APP

# View logs
oc logs -f deployment/backend -n shelfinity-dev
oc logs -f deployment/frontend -n shelfinity-dev
oc logs -f deployment/postgresql -n shelfinity-dev
oc logs -f deployment/keycloak -n shelfinity-dev
```

## 🧹 Cleanup (When Done Testing)

```bash
# Remove deployment but keep namespace
./openshift/scripts/cleanup.sh -n shelfinity-dev

# Remove everything including namespace
./openshift/scripts/cleanup.sh -n shelfinity-dev --delete-namespace

# Or use Helm directly
helm uninstall shelfinity -n shelfinity-dev
oc delete project shelfinity-dev
```

## 🐛 Troubleshooting

### If deployment script fails:

```bash
# Check script permissions
ls -la openshift/scripts/deploy.sh

# Make executable if needed
chmod +x openshift/scripts/deploy.sh

# Run with bash explicitly
bash openshift/scripts/deploy.sh -e dev
```

### If pods are not starting:

```bash
# Describe pod to see events
oc describe pod <pod-name> -n shelfinity-dev

# Check pod logs
oc logs <pod-name> -n shelfinity-dev

# Check events
oc get events -n shelfinity-dev --sort-by='.lastTimestamp'
```

### If URLs are not configured:

```bash
# Manually run URL configuration
cd /Users/amalrajjoseph/Shadow-Codex/Shelfinity

# Get routes
FRONTEND_URL="https://$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')"
BACKEND_URL="https://$(oc get route backend -n shelfinity-dev -o jsonpath='{.spec.host}')"
KEYCLOAK_URL="https://$(oc get route keycloak -n shelfinity-dev -o jsonpath='{.spec.host}')"

# Update configs
oc patch configmap backend-config -n shelfinity-dev \
  --type merge \
  -p "{\"data\":{\"FRONTEND_URL\":\"${FRONTEND_URL}\"}}"

oc patch configmap frontend-config -n shelfinity-dev \
  --type merge \
  -p "{\"data\":{\"REACT_APP_KEYCLOAK_URL\":\"${KEYCLOAK_URL}\"}}"

# Restart
oc rollout restart deployment/backend -n shelfinity-dev
oc rollout restart deployment/frontend -n shelfinity-dev
```

## 📞 Need Help?

- Check logs: `oc logs -f deployment/<service-name> -n shelfinity-dev`
- Check events: `oc get events -n shelfinity-dev`
- Check pod status: `oc describe pod <pod-name> -n shelfinity-dev`
- Review [FAQ](./FAQ.md)
- Review [Troubleshooting Guide](./README.md#troubleshooting)

---

**Your Working Directory**: `/Users/amalrajjoseph/Shadow-Codex/Shelfinity`  
**Deployment Script**: `./openshift/scripts/deploy.sh`  
**Helm Chart**: `./openshift/helm/shelfinity`