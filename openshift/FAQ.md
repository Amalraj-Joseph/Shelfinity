# Shelfinity OpenShift Deployment - FAQ

## Frequently Asked Questions

### 1. In which namespace will the project be deployed?

The deployment namespace is **configurable** and depends on the environment:

#### Default Namespaces:
- **Development**: `shelfinity-dev`
- **Staging**: `shelfinity-staging` (if using default values.yaml)
- **Production**: `shelfinity-prod`

#### How to Specify Namespace:

**Option 1: Using deployment script**
```bash
./openshift/scripts/deploy.sh -n my-custom-namespace -e dev
```

**Option 2: Using environment variable**
```bash
export NAMESPACE=my-custom-namespace
./openshift/scripts/deploy.sh -e dev
```

**Option 3: Using Helm directly**
```bash
helm install shelfinity ./openshift/helm/shelfinity \
  --namespace my-custom-namespace \
  --create-namespace \
  --set global.namespace=my-custom-namespace
```

#### Namespace Structure:
Each namespace contains:
- PostgreSQL database
- Keycloak authentication server
- Backend API service
- Frontend web application
- All associated ConfigMaps, Secrets, and Routes

---

### 2. Will the URLs be updated automatically by the deployment script?

**YES!** The URLs are automatically configured through a **post-install Kubernetes Job**.

#### How It Works:

1. **Helm deploys all resources** (Deployments, Services, Routes)
2. **Post-install Job runs automatically** after deployment
3. **Job discovers the actual Route URLs** created by OpenShift
4. **Job updates ConfigMaps** with the discovered URLs:
   - Backend gets `FRONTEND_URL` for CORS
   - Backend gets `OIDC_ISSUER` with Keycloak URL
   - Frontend gets `REACT_APP_KEYCLOAK_URL`
   - Frontend gets `REACT_APP_API_URL`
5. **Job restarts deployments** to apply new configuration
6. **Services start with correct URLs**

#### What Gets Configured:

```yaml
# Backend ConfigMap (automatically updated)
FRONTEND_URL: "https://frontend-shelfinity-dev.apps.cluster.com"
OIDC_ISSUER: "https://keycloak-shelfinity-dev.apps.cluster.com/realms/shelfinity"

# Frontend ConfigMap (automatically updated)
REACT_APP_KEYCLOAK_URL: "https://keycloak-shelfinity-dev.apps.cluster.com"
REACT_APP_API_URL: "https://backend-shelfinity-dev.apps.cluster.com"
```

#### Monitoring the Configuration:

```bash
# Watch the post-install job
oc logs job/shelfinity-post-install -n shelfinity-dev -f

# Verify ConfigMaps were updated
oc get configmap backend-config -n shelfinity-dev -o yaml
oc get configmap frontend-config -n shelfinity-dev -o yaml

# Check final routes
oc get routes -n shelfinity-dev
```

#### Manual URL Update (if needed):

If you need to manually update URLs:

```bash
# Get the routes
FRONTEND_URL=$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')
BACKEND_URL=$(oc get route backend -n shelfinity-dev -o jsonpath='{.spec.host}')
KEYCLOAK_URL=$(oc get route keycloak -n shelfinity-dev -o jsonpath='{.spec.host}')

# Update backend config
oc patch configmap backend-config -n shelfinity-dev \
  --type merge \
  -p "{\"data\":{\"FRONTEND_URL\":\"https://${FRONTEND_URL}\"}}"

# Update frontend config
oc patch configmap frontend-config -n shelfinity-dev \
  --type merge \
  -p "{\"data\":{\"REACT_APP_KEYCLOAK_URL\":\"https://${KEYCLOAK_URL}\",\"REACT_APP_API_URL\":\"https://${BACKEND_URL}\"}}"

# Restart deployments
oc rollout restart deployment/backend -n shelfinity-dev
oc rollout restart deployment/frontend -n shelfinity-dev
```

---

### 3. Will there be CORS issues?

**NO!** CORS is properly configured to prevent issues.

#### How CORS is Handled:

1. **Backend has a CorsFilter** (`backend/src/main/java/com/shelfinity/security/CorsFilter.java`)
2. **Filter reads `FRONTEND_URL` environment variable**
3. **Post-install job sets correct `FRONTEND_URL`**
4. **CORS headers are automatically added to all responses**

#### CORS Configuration Details:

```java
// From CorsFilter.java
Access-Control-Allow-Origin: https://frontend-shelfinity-dev.apps.cluster.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

#### What's Configured:

✅ **Allow-Origin**: Set to actual frontend URL (not wildcard)
✅ **Allow-Methods**: All required HTTP methods
✅ **Allow-Headers**: All headers (including Authorization)
✅ **Allow-Credentials**: Enabled for cookie/token support
✅ **Preflight Caching**: 1 hour to reduce OPTIONS requests

#### Verifying CORS Configuration:

```bash
# Check backend environment
oc exec deployment/backend -n shelfinity-dev -- env | grep FRONTEND_URL

# Test CORS from command line
FRONTEND_URL=$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')
BACKEND_URL=$(oc get route backend -n shelfinity-dev -o jsonpath='{.spec.host}')

curl -H "Origin: https://${FRONTEND_URL}" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     -v "https://${BACKEND_URL}/api/books"
```

Expected response headers:
```
< Access-Control-Allow-Origin: https://frontend-shelfinity-dev.apps.cluster.com
< Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
< Access-Control-Allow-Headers: *
< Access-Control-Allow-Credentials: true
```

#### Troubleshooting CORS Issues:

If you encounter CORS issues:

1. **Check FRONTEND_URL is set correctly:**
   ```bash
   oc get configmap backend-config -n shelfinity-dev -o jsonpath='{.data.FRONTEND_URL}'
   ```

2. **Verify backend pod has the environment variable:**
   ```bash
   oc exec deployment/backend -n shelfinity-dev -- env | grep FRONTEND_URL
   ```

3. **Check backend logs for CORS filter:**
   ```bash
   oc logs deployment/backend -n shelfinity-dev | grep -i cors
   ```

4. **Manually update if needed:**
   ```bash
   FRONTEND_URL=$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')
   oc set env deployment/backend FRONTEND_URL="https://${FRONTEND_URL}" -n shelfinity-dev
   ```

5. **Restart backend:**
   ```bash
   oc rollout restart deployment/backend -n shelfinity-dev
   ```

---

## Additional Common Questions

### 4. How do I access the deployed application?

```bash
# Get all routes
oc get routes -n shelfinity-dev

# Access URLs
echo "Frontend:  https://$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')"
echo "Backend:   https://$(oc get route backend -n shelfinity-dev -o jsonpath='{.spec.host}')"
echo "Keycloak:  https://$(oc get route keycloak -n shelfinity-dev -o jsonpath='{.spec.host}')"
```

### 5. How do I update the deployment?

```bash
# Update Helm values
vim openshift/helm/shelfinity/values-dev.yaml

# Upgrade deployment
helm upgrade shelfinity ./openshift/helm/shelfinity \
  -n shelfinity-dev \
  --values openshift/helm/shelfinity/values-dev.yaml
```

### 6. How do I scale the services?

```bash
# Scale backend
oc scale deployment/backend --replicas=3 -n shelfinity-dev

# Scale frontend
oc scale deployment/frontend --replicas=3 -n shelfinity-dev

# Or update values.yaml and helm upgrade
```

### 7. How do I check if everything is working?

```bash
# Check all pods
oc get pods -n shelfinity-dev

# Check services
oc get svc -n shelfinity-dev

# Check routes
oc get routes -n shelfinity-dev

# Check post-install job
oc get job shelfinity-post-install -n shelfinity-dev
oc logs job/shelfinity-post-install -n shelfinity-dev

# Test backend health
BACKEND_URL=$(oc get route backend -n shelfinity-dev -o jsonpath='{.spec.host}')
curl -k "https://${BACKEND_URL}/health"
```

### 8. What if the post-install job fails?

```bash
# Check job status
oc get job shelfinity-post-install -n shelfinity-dev

# View job logs
oc logs job/shelfinity-post-install -n shelfinity-dev

# Delete and re-run (Helm will recreate it)
oc delete job shelfinity-post-install -n shelfinity-dev
helm upgrade shelfinity ./openshift/helm/shelfinity -n shelfinity-dev --reuse-values

# Or manually configure URLs (see question 2)
```

### 9. How do I enable HTTPS/TLS?

**TLS is enabled by default!** All OpenShift Routes use edge termination:

```yaml
# Configured in route templates
tls:
  termination: edge
  insecureEdgeTerminationPolicy: Redirect
```

This means:
- ✅ All external traffic is HTTPS
- ✅ HTTP requests are redirected to HTTPS
- ✅ Internal pod-to-pod traffic is HTTP (within cluster)

### 10. Can I use custom domains?

Yes! Update the route hosts in values.yaml:

```yaml
backend:
  route:
    host: "api.mycompany.com"

frontend:
  route:
    host: "app.mycompany.com"

keycloak:
  route:
    host: "auth.mycompany.com"
```

Then configure DNS to point to your OpenShift router.

---

## Need More Help?

- Check the main [README](./README.md)
- Review [OpenShift documentation](https://docs.openshift.com)
- Check pod logs: `oc logs <pod-name> -n <namespace>`
- Describe resources: `oc describe <resource-type> <resource-name> -n <namespace>`

---

**Last Updated**: 2026-03-21