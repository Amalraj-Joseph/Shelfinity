# Shelfinity OpenShift Deployment Guide

Complete guide for deploying Shelfinity Library Management System on OpenShift using Helm charts.

## 📋 Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Deployment Options](#deployment-options)
- [Configuration](#configuration)
- [Build and Deploy](#build-and-deploy)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)
- [Cleanup](#cleanup)

## 🎯 Overview

This deployment uses:
- **Helm 3** for package management
- **OpenShift BuildConfigs** for building container images from source
- **OpenShift Routes** for external access
- **PostgreSQL** for database
- **Keycloak** for authentication
- **Network Policies** for security

## ✅ Prerequisites

### Required Tools
- OpenShift CLI (`oc`) version 4.10+
- Helm 3.8+
- Git (for source builds)

### OpenShift Cluster
- OpenShift 4.10+ cluster
- Cluster admin or project admin permissions
- Sufficient resources:
  - **Dev**: 4 CPU, 8GB RAM
  - **Prod**: 12 CPU, 24GB RAM

### Access Requirements
- Logged into OpenShift cluster
- Git repository access (for builds)

## 🏗️ Architecture

### Microservices Components

```
┌─────────────────────────────────────────────────────────┐
│                    OpenShift Cluster                     │
│                                                          │
│  ┌──────────────┐    ┌──────────────┐                  │
│  │   Frontend   │◄───┤  Route (TLS) │                  │
│  │   (React)    │    └──────────────┘                  │
│  └──────┬───────┘                                       │
│         │                                               │
│         ▼                                               │
│  ┌──────────────┐    ┌──────────────┐                  │
│  │   Keycloak   │◄───┤  Route (TLS) │                  │
│  │    (Auth)    │    └──────────────┘                  │
│  └──────┬───────┘                                       │
│         │                                               │
│         ▼                                               │
│  ┌──────────────┐    ┌──────────────┐                  │
│  │   Backend    │◄───┤  Route (TLS) │                  │
│  │ (Jakarta EE) │    └──────────────┘                  │
│  └──────┬───────┘                                       │
│         │                                               │
│         ▼                                               │
│  ┌──────────────┐                                       │
│  │  PostgreSQL  │                                       │
│  │  (Database)  │                                       │
│  └──────────────┘                                       │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### Build Process

```
Git Repository
     │
     ▼
BuildConfig (S2I/Docker)
     │
     ▼
ImageStream
     │
     ▼
Deployment
```

## 🚀 Quick Start

### 1. Login to OpenShift

```bash
oc login https://api.your-cluster.com:6443
```

### 2. Deploy to Development

```bash
cd openshift/scripts
./deploy.sh -e dev -g https://github.com/your-org/shelfinity.git
```

### 3. Access the Application

```bash
# Get routes
oc get routes -n shelfinity-dev

# Example output:
# NAME       HOST                                    TLS
# frontend   frontend-shelfinity-dev.apps.cluster    edge
# backend    backend-shelfinity-dev.apps.cluster     edge
# keycloak   keycloak-shelfinity-dev.apps.cluster    edge
```

## 📦 Deployment Options

### Option 1: Using Deployment Script (Recommended)

```bash
# Deploy to dev environment
./openshift/scripts/deploy.sh -e dev

# Deploy to production
./openshift/scripts/deploy.sh -e prod -n shelfinity-prod

# Deploy with custom git repository
./openshift/scripts/deploy.sh \
  -e dev \
  -g https://github.com/your-org/shelfinity.git \
  -b main
```

### Option 2: Using Helm Directly

```bash
# Create namespace
oc new-project shelfinity-dev

# Install with Helm
helm install shelfinity ./openshift/helm/shelfinity \
  --namespace shelfinity-dev \
  --values ./openshift/helm/shelfinity/values-dev.yaml \
  --set global.namespace=shelfinity-dev
```

### Option 3: Manual Deployment

```bash
# Apply templates manually
oc apply -f openshift/helm/shelfinity/templates/ -n shelfinity-dev
```

## ⚙️ Configuration

### Environment-Specific Values

Three environment configurations are provided:

#### Development (`values-dev.yaml`)
- Single replica for all services
- Minimal resources
- No autoscaling
- Local storage

#### Staging (`values.yaml`)
- 2 replicas for services
- Moderate resources
- Optional autoscaling
- Persistent storage

#### Production (`values-prod.yaml`)
- 3+ replicas for services
- High resources
- Autoscaling enabled
- Production-grade storage

### Key Configuration Parameters

```yaml
# Global settings
global:
  namespace: shelfinity-dev
  environment: dev
  domain: apps.cluster.example.com

# Database
database:
  persistence:
    enabled: true
    size: 5Gi
    storageClass: "gp3-csi"

# Backend
backend:
  replicas: 2
  resources:
    requests:
      memory: "512Mi"
      cpu: "500m"
    limits:
      memory: "1Gi"
      cpu: "1000m"

# Autoscaling
autoscaling:
  backend:
    enabled: true
    minReplicas: 2
    maxReplicas: 5
```

### Customizing Values

Create a custom values file:

```bash
# Copy base values
cp openshift/helm/shelfinity/values.yaml my-values.yaml

# Edit as needed
vim my-values.yaml

# Deploy with custom values
helm install shelfinity ./openshift/helm/shelfinity \
  --namespace shelfinity-dev \
  --values my-values.yaml
```

## 🔨 Build and Deploy

### Triggering Builds

Builds are automatically triggered when:
1. BuildConfig is created
2. Source code changes (webhook)
3. Manual trigger

#### Manual Build Trigger

```bash
# Trigger backend build
oc start-build backend -n shelfinity-dev --follow

# Trigger frontend build
oc start-build frontend -n shelfinity-dev --follow

# Build from specific branch
oc start-build backend \
  -n shelfinity-dev \
  --env=GIT_BRANCH=feature-branch \
  --follow
```

### Monitoring Builds

```bash
# List builds
oc get builds -n shelfinity-dev

# Watch build logs
oc logs -f build/backend-1 -n shelfinity-dev

# Get build status
oc get build backend-1 -n shelfinity-dev -o jsonpath='{.status.phase}'
```

### Image Management

```bash
# List image streams
oc get imagestreams -n shelfinity-dev

# View image tags
oc describe imagestream shelfinity-backend -n shelfinity-dev

# Tag image for promotion
oc tag shelfinity-dev/shelfinity-backend:latest \
  shelfinity-prod/shelfinity-backend:v1.0.0
```

## 📊 Monitoring

### Health Checks

```bash
# Check pod status
oc get pods -n shelfinity-dev

# Check deployment status
oc get deployments -n shelfinity-dev

# View pod logs
oc logs -f deployment/backend -n shelfinity-dev
```

### Application Health Endpoints

- **Backend Liveness**: `https://backend-url/health/live`
- **Backend Readiness**: `https://backend-url/health/ready`
- **Backend Metrics**: `https://backend-url/metrics`

### Resource Usage

```bash
# View resource usage
oc adm top pods -n shelfinity-dev

# View node allocation
oc adm top nodes
```

## 🔍 Troubleshooting

### Common Issues

#### 1. Pods Not Starting

```bash
# Check pod events
oc describe pod <pod-name> -n shelfinity-dev

# Check pod logs
oc logs <pod-name> -n shelfinity-dev

# Check previous container logs
oc logs <pod-name> -n shelfinity-dev --previous
```

#### 2. Build Failures

```bash
# Check build logs
oc logs build/<build-name> -n shelfinity-dev

# Describe build
oc describe build/<build-name> -n shelfinity-dev

# Retry failed build
oc start-build --from-build=<build-name> -n shelfinity-dev
```

#### 3. Database Connection Issues

```bash
# Test database connectivity
oc rsh deployment/backend -n shelfinity-dev
# Inside pod:
curl postgresql:5432

# Check database logs
oc logs deployment/postgresql -n shelfinity-dev
```

#### 4. Route/Ingress Issues

```bash
# Check routes
oc get routes -n shelfinity-dev

# Describe route
oc describe route frontend -n shelfinity-dev

# Test route
curl -k https://$(oc get route frontend -n shelfinity-dev -o jsonpath='{.spec.host}')
```

### Debug Mode

Enable debug logging:

```bash
# Update deployment with debug env
oc set env deployment/backend LOG_LEVEL=DEBUG -n shelfinity-dev

# View logs
oc logs -f deployment/backend -n shelfinity-dev
```

## 🧹 Cleanup

### Remove Deployment (Keep Namespace)

```bash
./openshift/scripts/cleanup.sh -n shelfinity-dev
```

### Remove Everything (Including Namespace)

```bash
./openshift/scripts/cleanup.sh -n shelfinity-dev --delete-namespace
```

### Manual Cleanup

```bash
# Uninstall Helm release
helm uninstall shelfinity -n shelfinity-dev

# Delete namespace
oc delete project shelfinity-dev
```

## 📚 Additional Resources

### Helm Commands

```bash
# List releases
helm list -n shelfinity-dev

# Get release values
helm get values shelfinity -n shelfinity-dev

# Upgrade release
helm upgrade shelfinity ./openshift/helm/shelfinity \
  -n shelfinity-dev \
  --values values-dev.yaml

# Rollback release
helm rollback shelfinity 1 -n shelfinity-dev
```

### OpenShift Commands

```bash
# Scale deployment
oc scale deployment/backend --replicas=3 -n shelfinity-dev

# Update image
oc set image deployment/backend \
  backend=image-registry.openshift-image-registry.svc:5000/shelfinity-dev/shelfinity-backend:v2 \
  -n shelfinity-dev

# Create route
oc expose service backend -n shelfinity-dev

# Port forward for local testing
oc port-forward service/backend 9080:9080 -n shelfinity-dev
```

## 🔐 Security Considerations

1. **Secrets Management**: Use OpenShift Secrets or external secret managers
2. **Network Policies**: Enabled by default, restricts pod-to-pod communication
3. **RBAC**: Use service accounts with minimal permissions
4. **TLS**: All routes use edge termination by default
5. **Image Security**: Scan images before deployment

## 📞 Support

For issues or questions:
- Check the [main README](../README.md)
- Review [troubleshooting section](#troubleshooting)
- Open an issue in the repository

---

**Last Updated**: 2026-03-21  
**Version**: 1.0.0