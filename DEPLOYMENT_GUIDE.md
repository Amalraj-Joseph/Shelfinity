# Shelfinity Deployment Guide

This guide explains how to deploy Shelfinity to different environments beyond localhost.

## Table of Contents
- [Environment Variables](#environment-variables)
- [Local Development](#local-development)
- [Production Deployment](#production-deployment)
- [Cloud Deployment](#cloud-deployment)
- [Security Considerations](#security-considerations)

---

## Environment Variables

Shelfinity uses environment variables for configuration to support deployment anywhere. See `.env.example` for all available variables.

### Required Variables

**Backend:**
- `DB_HOST` - Database hostname
- `DB_PORT` - Database port (default: 5432)
- `DB_NAME` - Database name
- `DB_USER` - Database username
- `DB_PASSWORD` - Database password
- `OIDC_ISSUER` - Keycloak issuer URL
- `FRONTEND_URL` - Frontend URL for CORS

**Frontend:**
- `REACT_APP_KEYCLOAK_URL` - Keycloak URL accessible from browser
- `REACT_APP_REALM` - Keycloak realm name
- `REACT_APP_CLIENT_ID` - Keycloak client ID

---

## Local Development

### Using Docker Compose (Default)

1. Start all services:
```bash
docker-compose -f docker/docker-compose.yml up -d
```

2. Access the application:
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:9080
   - Keycloak: http://localhost:8080

### Custom Configuration

Create a `.env` file in the project root:
```bash
cp .env.example .env
# Edit .env with your values
```

Update `docker-compose.yml` to use the `.env` file:
```yaml
env_file:
  - ../.env
```

---

## Production Deployment

### Prerequisites
- Domain name with DNS configured
- SSL/TLS certificates
- Production database
- Secure credential management

### Step 1: Update Environment Variables

Create production environment file:
```bash
# Production values
FRONTEND_URL=https://library.yourdomain.com
BACKEND_URL=https://api.yourdomain.com
KEYCLOAK_URL=https://auth.yourdomain.com

DB_HOST=your-db-host
DB_PASSWORD=secure-password-here

REACT_APP_KEYCLOAK_URL=https://auth.yourdomain.com
```

### Step 2: Update Keycloak Realm Configuration

Update `docker/keycloak/realm-shelfinity.json`:
```json
{
  "clients": [
    {
      "clientId": "shelfinity-frontend",
      "redirectUris": [
        "https://library.yourdomain.com/*"
      ],
      "webOrigins": [
        "https://library.yourdomain.com"
      ]
    }
  ]
}
```

### Step 3: Configure SSL/TLS

Add reverse proxy (nginx/traefik) with SSL:
```nginx
server {
    listen 443 ssl http2;
    server_name library.yourdomain.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location / {
        proxy_pass http://frontend:80;
    }
    
    location /api/ {
        proxy_pass http://backend:9080/shelfinity-backend/app/;
    }
}
```

### Step 4: Deploy

```bash
docker-compose -f docker/docker-compose.yml up -d --build
```

---

## Cloud Deployment

### AWS ECS/Fargate

1. Create ECR repositories for frontend and backend
2. Build and push images:
```bash
docker build -t shelfinity-backend ./backend
docker tag shelfinity-backend:latest <account>.dkr.ecr.<region>.amazonaws.com/shelfinity-backend:latest
docker push <account>.dkr.ecr.<region>.amazonaws.com/shelfinity-backend:latest
```

3. Create ECS task definitions with environment variables
4. Use AWS RDS for PostgreSQL
5. Configure Application Load Balancer
6. Set up Route53 for DNS

### Kubernetes

1. Create ConfigMaps for configuration:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: shelfinity-config
data:
  DB_HOST: "postgres-service"
  DB_PORT: "5432"
  FRONTEND_URL: "https://library.yourdomain.com"
```

2. Create Secrets for sensitive data:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: shelfinity-secrets
type: Opaque
stringData:
  DB_PASSWORD: "your-secure-password"
```

3. Deploy services:
```bash
kubectl apply -f k8s/
```

### Google Cloud Run

1. Build container images
2. Push to Google Container Registry
3. Deploy with environment variables:
```bash
gcloud run deploy shelfinity-backend \
  --image gcr.io/project/shelfinity-backend \
  --set-env-vars DB_HOST=<cloud-sql-host>,FRONTEND_URL=https://yourdomain.com
```

---

## Security Considerations

### Production Checklist

- [ ] Change all default passwords
- [ ] Use strong, unique passwords for all services
- [ ] Enable SSL/TLS for all connections
- [ ] Use environment variables or secrets management (never commit credentials)
- [ ] Configure firewall rules (only expose necessary ports)
- [ ] Enable database encryption at rest
- [ ] Set up regular backups
- [ ] Configure monitoring and logging
- [ ] Use HTTPS-only cookies
- [ ] Implement rate limiting
- [ ] Keep all dependencies updated
- [ ] Review and update CORS settings
- [ ] Enable Keycloak security features (MFA, password policies)

### Secrets Management

**Recommended Tools:**
- AWS Secrets Manager
- HashiCorp Vault
- Azure Key Vault
- Google Secret Manager
- Kubernetes Secrets

**Example with AWS Secrets Manager:**
```bash
# Store secret
aws secretsmanager create-secret \
  --name shelfinity/db-password \
  --secret-string "your-secure-password"

# Retrieve in application
DB_PASSWORD=$(aws secretsmanager get-secret-value \
  --secret-id shelfinity/db-password \
  --query SecretString --output text)
```

---

## Monitoring

### Health Checks

- Backend: `http://backend:9080/health`
- Frontend: `http://frontend:80`

### Logging

Configure centralized logging:
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Splunk
- CloudWatch (AWS)
- Stackdriver (GCP)

### Metrics

Monitor:
- Response times
- Error rates
- Database connections
- Memory/CPU usage
- Active users

---

## Troubleshooting

### Common Issues

**CORS Errors:**
- Verify `FRONTEND_URL` environment variable is set correctly
- Check browser console for actual origin
- Ensure Keycloak redirectUris match your domain

**Database Connection Failed:**
- Verify `DB_HOST`, `DB_PORT` are correct
- Check network connectivity
- Verify credentials
- Check database is running

**Keycloak Authentication Failed:**
- Verify `REACT_APP_KEYCLOAK_URL` is accessible from browser
- Check realm configuration
- Verify client settings
- Check browser console for errors

---

## Support

For issues or questions:
1. Check the logs: `docker-compose logs -f [service-name]`
2. Review this guide and README.md
3. Check GitHub issues
4. Contact support team

---

**Last Updated:** 2026-03-21