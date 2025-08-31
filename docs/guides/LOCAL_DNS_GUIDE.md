# Local DNS Setup Guide

## Overview

This guide explains how to set up local DNS entries for the Shelfinity Library Management System. This allows you to access the application using custom domain names instead of localhost.

## Prerequisites

- Linux/macOS system (Windows users may need different steps)
- Administrative privileges (sudo access)
- Shelfinity application running

## Quick Setup

### Option 1: Using the Setup Script (Recommended)

1. Run the DNS setup script:
   ```bash
   sudo ./scripts/setup-local-dns.sh
   ```

2. Start the application with DNS support:
   ```bash
   ./scripts/start-with-dns.sh
   ```

### Option 2: Manual Setup

1. Edit the hosts file:
   ```bash
   sudo nano /etc/hosts
   ```

2. Add the following entries:
   ```
   127.0.0.1 shelfinity.local
   127.0.0.1 www.shelfinity.local
   127.0.0.1 keycloak.local
   ```

3. Save the file and exit

## Access URLs

After setup, you can access the services using:

- **Frontend**: http://shelfinity.local:3000
- **Backend API**: http://shelfinity.local:9080/api
- **Keycloak Admin**: http://keycloak.local:8080
- **API Documentation**: http://shelfinity.local:3000/docs

## Platform-Specific Instructions

### Linux

1. Open terminal and run:
   ```bash
   sudo nano /etc/hosts
   ```

2. Add the DNS entries as shown above

3. Test the setup:
   ```bash
   ping shelfinity.local
   ping keycloak.local
   ```

### macOS

1. Open terminal and run:
   ```bash
   sudo nano /etc/hosts
   ```

2. Add the DNS entries as shown above

3. Flush DNS cache:
   ```bash
   sudo dscacheutil -flushcache
   sudo killall -HUP mDNSResponder
   ```

### Windows

1. Open Command Prompt as Administrator

2. Edit the hosts file:
   ```cmd
   notepad C:\Windows\System32\drivers\etc\hosts
   ```

3. Add the DNS entries as shown above

4. Flush DNS cache:
   ```cmd
   ipconfig /flushdns
   ```

## Verification

### Test DNS Resolution

1. Test with ping:
   ```bash
   ping shelfinity.local
   ping keycloak.local
   ```

2. Test with curl:
   ```bash
   curl -I http://shelfinity.local:3000
   curl -I http://keycloak.local:8080
   ```

### Test Application Access

1. Open your browser and navigate to http://shelfinity.local:3000
2. Verify the application loads correctly
3. Test the login functionality
4. Verify Keycloak admin console at http://keycloak.local:8080

## Troubleshooting

### Common Issues

1. **DNS Not Resolving**
   - Check if hosts file entries are correct
   - Verify no typos in domain names
   - Ensure hosts file has proper permissions

2. **Browser Caching**
   - Clear browser cache and cookies
   - Try incognito/private browsing mode
   - Restart the browser

3. **Port Conflicts**
   - Ensure ports 3000, 8080, and 9080 are available
   - Check if other services are using these ports

4. **Firewall Issues**
   - Ensure firewall allows local connections
   - Check if antivirus software is blocking connections

### Debug Commands

```bash
# Check hosts file
cat /etc/hosts | grep shelfinity

# Test DNS resolution
nslookup shelfinity.local
dig shelfinity.local

# Check if ports are listening
netstat -tulpn | grep :3000
netstat -tulpn | grep :8080
netstat -tulpn | grep :9080

# Test connectivity
telnet shelfinity.local 3000
telnet keycloak.local 8080
```

## Security Considerations

1. **Local Only**
   - These DNS entries only work on your local machine
   - They don't affect other systems on your network

2. **Development Use**
   - This setup is intended for development only
   - Use proper DNS for production environments

3. **Cleanup**
   - Remove entries when no longer needed
   - Don't leave unnecessary entries in hosts file

## Cleanup

To remove the DNS entries:

```bash
# Remove entries from hosts file
sudo sed -i '/shelfinity.local\|keycloak.local/d' /etc/hosts
```

Or manually edit `/etc/hosts` and remove the added lines.

## Alternative Approaches

### Using dnsmasq (Advanced)

For more advanced DNS management:

1. Install dnsmasq:
   ```bash
   sudo apt-get install dnsmasq  # Ubuntu/Debian
   sudo brew install dnsmasq     # macOS
   ```

2. Configure dnsmasq:
   ```bash
   sudo nano /etc/dnsmasq.conf
   ```

3. Add configuration:
   ```
   address=/shelfinity.local/127.0.0.1
   address=/keycloak.local/127.0.0.1
   ```

4. Start dnsmasq:
   ```bash
   sudo systemctl start dnsmasq
   ```

### Using Docker DNS

For containerized environments:

1. Create a custom Docker network:
   ```bash
   docker network create shelfinity-network
   ```

2. Add DNS aliases to containers:
   ```yaml
   networks:
     shelfinity-network:
       aliases:
         - shelfinity.local
         - keycloak.local
   ```

## Next Steps

After setting up local DNS:

1. Configure Keycloak with the new domain names
2. Update application configuration if needed
3. Test all functionality with the new URLs
4. Set up SSL certificates for HTTPS (optional)

For more information, refer to the main [README.md](../../README.md) and [Keycloak Setup Guide](KEYCLOAK_SETUP.md).
