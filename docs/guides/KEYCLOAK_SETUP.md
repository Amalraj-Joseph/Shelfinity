# Keycloak Setup Guide

## Overview

This guide explains how to configure Keycloak for the Shelfinity Library Management System. Keycloak provides enterprise-grade identity and access management for the application.

## Prerequisites

- Shelfinity application running (see main README.md for setup instructions)
- Access to Keycloak Admin Console at http://localhost:8080
- Admin credentials: `admin/admin`

## Initial Setup

### 1. Access Keycloak Admin Console

1. Open your browser and navigate to http://localhost:8080
2. Click on "Administration Console"
3. Login with:
   - Username: `admin`
   - Password: `admin`

### 2. Create a New Realm

1. In the top-left corner, click on the dropdown showing "master"
2. Click "Create Realm"
3. Enter the following details:
   - **Realm name**: `shelfinity`
   - **Display name**: `Shelfinity Library Management`
   - **Enabled**: ✓ (checked)
4. Click "Create"

### 3. Create a Client

1. In the left sidebar, click "Clients"
2. Click "Create"
3. Enter the following details:
   - **Client ID**: `shelfinity-frontend`
   - **Client Protocol**: `openid-connect`
   - **Root URL**: `http://localhost:3000`
4. Click "Save"

### 4. Configure Client Settings

1. In the client settings, go to the "Settings" tab
2. Configure the following:
   - **Access Type**: `public`
   - **Valid Redirect URIs**: `http://localhost:3000/*`
   - **Web Origins**: `http://localhost:3000`
   - **Admin URL**: `http://localhost:3000`
3. Click "Save"

### 5. Create Roles

1. In the left sidebar, click "Roles"
2. Click "Add Role"
3. Create the following roles:

#### Admin Role
- **Role Name**: `admin`
- **Description**: `Administrator role with full access`
- **Composite Roles**: Leave unchecked
- **Client Roles**: Leave unchecked

#### User Role
- **Role Name**: `user`
- **Description**: `Regular user role`
- **Composite Roles**: Leave unchecked
- **Client Roles**: Leave unchecked

### 6. Create Users

1. In the left sidebar, click "Users"
2. Click "Add User"

#### Create Admin User
1. Enter the following details:
   - **Username**: `admin`
   - **Email**: `admin@shelfinity.local`
   - **First Name**: `Admin`
   - **Last Name**: `User`
   - **Email Verified**: ✓ (checked)
   - **Enabled**: ✓ (checked)
2. Click "Save"
3. Go to the "Credentials" tab
4. Set password: `admin123`
5. Set "Temporary" to "OFF"
6. Click "Set Password"
7. Go to the "Role Mappings" tab
8. Under "Realm Roles", add the `admin` role

#### Create Regular User
1. Click "Add User" again
2. Enter the following details:
   - **Username**: `user`
   - **Email**: `user@shelfinity.local`
   - **First Name**: `Regular`
   - **Last Name**: `User`
   - **Email Verified**: ✓ (checked)
   - **Enabled**: ✓ (checked)
3. Click "Save"
4. Go to the "Credentials" tab
5. Set password: `user123`
6. Set "Temporary" to "OFF"
7. Click "Set Password"
8. Go to the "Role Mappings" tab
9. Under "Realm Roles", add the `user` role

## Advanced Configuration

### 1. Configure Identity Providers (Optional)

To enable Google authentication:

1. In the left sidebar, click "Identity Providers"
2. Click "Add provider"
3. Select "Google"
4. Configure with your Google OAuth credentials
5. Set "Display Name" to "Google"
6. Enable "Trust Email"
7. Click "Save"

### 2. Configure Email Settings

1. In the left sidebar, click "Realm Settings"
2. Go to the "Email" tab
3. Configure your SMTP settings:
   - **Host**: Your SMTP server
   - **Port**: SMTP port (usually 587 or 465)
   - **From**: `noreply@shelfinity.local`
   - **Username**: Your SMTP username
   - **Password**: Your SMTP password
4. Click "Save"
5. Test the configuration

### 3. Configure Password Policy

1. In the left sidebar, click "Realm Settings"
2. Go to the "Authentication" tab
3. Click "Password Policy"
4. Add policies as needed:
   - `length(8)` - Minimum 8 characters
   - `uppercase(1)` - At least 1 uppercase letter
   - `lowercase(1)` - At least 1 lowercase letter
   - `digits(1)` - At least 1 digit
   - `specialChars(1)` - At least 1 special character

## Testing the Configuration

### 1. Test User Login

1. Open http://localhost:3000
2. Click "Sign In"
3. Try logging in with the created users:
   - Admin: `admin` / `admin123`
   - User: `user` / `user123`

### 2. Test Role-Based Access

1. Login as admin user
2. Verify you can access the admin panel
3. Login as regular user
4. Verify admin panel is not accessible

### 3. Test API Authentication

1. Use a tool like Postman or curl
2. Make a request to `http://localhost:9080/api/users`
3. Include the JWT token in the Authorization header
4. Verify the response

## Troubleshooting

### Common Issues

1. **Login Redirect Loop**
   - Check Valid Redirect URIs in client settings
   - Ensure Web Origins is configured correctly

2. **CORS Errors**
   - Verify Web Origins includes your frontend URL
   - Check browser console for specific CORS errors

3. **JWT Token Issues**
   - Verify client secret is configured correctly
   - Check token expiration settings

4. **Role Mapping Issues**
   - Ensure roles are assigned to users
   - Check if roles are included in JWT tokens

### Debug Mode

To enable debug logging:

1. In Keycloak Admin Console, go to "Realm Settings"
2. Go to the "Logs" tab
3. Set "Root Logger" to "DEBUG"
4. Click "Save"

### Useful Commands

```bash
# Check Keycloak logs
docker logs shelfinity-keycloak

# Restart Keycloak
docker restart shelfinity-keycloak

# Access Keycloak container
docker exec -it shelfinity-keycloak /bin/bash
```

## Security Considerations

1. **Change Default Passwords**
   - Change the admin password after initial setup
   - Use strong passwords for all users

2. **Enable HTTPS**
   - Configure SSL certificates for production
   - Use HTTPS URLs in client configuration

3. **Regular Updates**
   - Keep Keycloak updated to the latest version
   - Monitor security advisories

4. **Backup Configuration**
   - Export realm configuration regularly
   - Backup user data and settings

## Next Steps

After completing the Keycloak setup:

1. Test the complete authentication flow
2. Configure additional identity providers if needed
3. Set up email verification and password reset
4. Configure session management and token policies
5. Set up monitoring and logging

For more information, refer to the [Keycloak Documentation](https://www.keycloak.org/documentation).
