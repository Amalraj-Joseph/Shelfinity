# Shelfinity OpenShift - Quick Reference

## 🚀 Quick Commands

### Deployment

```bash
# Deploy to dev
./openshift/scripts/deploy.sh -e dev -g https://github.com/your-org/shelfinity.git

# Deploy to prod
./openshift/scripts/deploy.sh -e prod -n shelfinity-prod -g https://github.com/your-org/shelfinity.git

# Deploy with custom namespace
./openshift/scripts/deploy.sh -n my-namespace -e dev
```

### Access URLs

```bash
# Get all routes
oc get routes -n shelfinity-dev

# Get specific URLs
echo "Frontend:  https://$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')"
echo "Backend:   https://$(oc get route backend -n shelfinity-dev -o jsonpath='{.spec.host}')"
echo "Keycloak:  https://$(oc get route keycloak -n shelfinity-dev -o jsonpath='{.spec.host}')"
```

### Status Checks

```bash
# Check all pods
oc get pods -n shelfinity-dev

# Check deployments
oc get deployments -n shelfinity-dev

# Check post-install job
oc get job shelfinity-post-install -n shelfinity-dev
oc logs job/shelfinity-post-install -n shelfinity-dev

# Check if URLs are configured
oc get configmap backend-config -n shelfinity-dev -o jsonpath='{.data.FRONTEND_URL}'
oc get configmap frontend-config -n shelfinity-dev -o jsonpath='{.data.REACT_APP_KEYCLOAK_URL}'
```

### Logs

```bash
# View backend logs
oc logs -f deployment/backend -n shelfinity-dev

# View frontend logs
oc logs -f deployment/frontend -n shelfinity-dev

# View database logs
oc logs -f deployment/postgresql -n shelfinity-dev

# View keycloak logs
oc logs -f deployment/keycloak -n shelfinity-dev
```

### Builds

```bash
# Trigger backend build
oc start-build backend -n shelfinity-dev --follow

# Trigger frontend build
oc start-build frontend -n shelfinity-dev --follow

# List builds
oc get builds -n shelfinity-dev

# View build logs
oc logs build/backend-1 -n shelfinity-dev
```

### Scaling

```bash
# Scale backend
oc scale deployment/backend --replicas=3 -n shelfinity-dev

# Scale frontend
oc scale deployment/frontend --replicas=3 -n shelfinity-dev

# Check current replicas
oc get deployment -n shelfinity-dev
```

### Troubleshooting

```bash
# Describe pod (shows events)
oc describe pod <pod-name> -n shelfinity-dev

# Get pod events
oc get events -n shelfinity-dev --sort-by='.lastTimestamp'

# Check resource usage
oc adm top pods -n shelfinity-dev

# Restart deployment
oc rollout restart deployment/backend -n shelfinity-dev

# Check rollout status
oc rollout status deployment/backend -n shelfinity-dev
```

### Configuration Updates

```bash
# Update backend config
oc edit configmap backend-config -n shelfinity-dev

# Update frontend config
oc edit configmap frontend-config -n shelfinity-dev

# Restart to apply changes
oc rollout restart deployment/backend -n shelfinity-dev
oc rollout restart deployment/frontend -n shelfinity-dev
```

### Cleanup

```bash
# Remove deployment (keep namespace)
./openshift/scripts/cleanup.sh -n shelfinity-dev

# Remove everything including namespace
./openshift/scripts/cleanup.sh -n shelfinity-dev --delete-namespace

# Quick cleanup with Helm
helm uninstall shelfinity -n shelfinity-dev
```

## 📋 Default Values

| Component | Default Namespace | Default Replicas | Default Resources |
|-----------|------------------|------------------|-------------------|
| PostgreSQL | shelfinity-dev | 1 | 256Mi/250m |
| Keycloak | shelfinity-dev | 1 | 512Mi/500m |
| Backend | shelfinity-dev | 2 | 512Mi/500m |
| Frontend | shelfinity-dev | 2 | 128Mi/100m |

## 🔗 Important URLs

After deployment, access:
- **Frontend**: `https://frontend-<namespace>.<cluster-domain>`
- **Backend API**: `https://backend-<namespace>.<cluster-domain>`
- **Keycloak**: `https://keycloak-<namespace>.<cluster-domain>`
- **Backend Health**: `https://backend-<namespace>.<cluster-domain>/health`
- **API Docs**: `https://backend-<namespace>.<cluster-domain>/openapi/ui`

## 🔐 Default Credentials

### Keycloak Admin
- Username: `admin`
- Password: Check secret `keycloak-secret`

### Database
- Database: `shelfinity`
- Username: `shelfinity`
- Password: Check secret `postgresql-secret`

## 📊 Health Checks

```bash
# Backend health
BACKEND_URL=$(oc get route backend -n shelfinity-dev -o jsonpath='{.spec.host}')
curl -k "https://${BACKEND_URL}/health"
curl -k "https://${BACKEND_URL}/health/live"
curl -k "https://${BACKEND_URL}/health/ready"

# Frontend health
FRONTEND_URL=$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')
curl -k "https://${FRONTEND_URL}/"
```

## 🐛 Common Issues

### Pods Not Starting
```bash
oc describe pod <pod-name> -n shelfinity-dev
oc logs <pod-name> -n shelfinity-dev
```

### CORS Errors
```bash
# Check FRONTEND_URL is set
oc get configmap backend-config -n shelfinity-dev -o jsonpath='{.data.FRONTEND_URL}'

# Update if needed
FRONTEND_URL=$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')
oc patch configmap backend-config -n shelfinity-dev \
  --type merge \
  -p "{\"data\":{\"FRONTEND_URL\":\"https://${FRONTEND_URL}\"}}"
oc rollout restart deployment/backend -n shelfinity-dev
```

### Build Failures
```bash
oc logs build/<build-name> -n shelfinity-dev
oc start-build --from-build=<build-name> -n shelfinity-dev
```

### Database Connection Issues
```bash
oc logs deployment/postgresql -n shelfinity-dev
oc rsh deployment/backend -n shelfinity-dev
# Inside pod: curl postgresql:5432
```

## 📚 More Information

- [Full README](./README.md) - Complete deployment guide
- [FAQ](./FAQ.md) - Frequently asked questions
- [Helm Values](./helm/shelfinity/values.yaml) - Configuration options

---

**Tip**: Bookmark this page for quick access to common commands!