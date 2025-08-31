# 🌐 Local DNS Setup Guide for Shelfinity

This guide explains how to set up local DNS so you can access Shelfinity using `shelfinity.com` instead of `localhost`.

## 🎯 Why Use Local DNS?

Using a custom domain like `shelfinity.com` instead of `localhost` provides:
- More professional development experience
- Easier to remember URLs
- Better simulation of production environment
- Consistent with real-world deployment scenarios

## 🚀 Quick Setup

### Option 1: Automatic Setup (Recommended)

```bash
# Run the startup script with DNS setup
sudo ./start-with-dns.sh
```

This script will:
1. Add `shelfinity.com` to your `/etc/hosts` file
2. Start all Docker services
3. Configure the system to use the custom domain

### Option 2: Manual Setup

```bash
# 1. Setup local DNS
sudo ./setup-local-dns.sh

# 2. Start the services
./start.sh
```

## 🔧 Manual DNS Configuration

If you prefer to configure DNS manually:

### Linux/macOS

1. Edit the hosts file:
   ```bash
   sudo nano /etc/hosts
   ```

2. Add these lines:
   ```
   127.0.0.1 shelfinity.com
   127.0.0.1 www.shelfinity.com
   ```

3. Save and exit

### Windows

1. Open Notepad as Administrator
2. Open `C:\Windows\System32\drivers\etc\hosts`
3. Add these lines:
   ```
   127.0.0.1 shelfinity.com
   127.0.0.1 www.shelfinity.com
   ```
4. Save the file

## 🌐 Access URLs

After DNS setup, you can access Shelfinity at:

| Service | URL |
|---------|-----|
| Frontend | http://shelfinity.com:3000 |
| Backend API | http://shelfinity.com:9080/api |
| Keycloak Admin | http://shelfinity.com:8080 |
| Health Check | http://shelfinity.com:9080/api/health |

## 🔄 Switching Between localhost and shelfinity.com

### Using localhost (Default)
```bash
./start.sh
```

### Using shelfinity.com
```bash
sudo ./start-with-dns.sh
```

## 🧪 Testing DNS Resolution

Test if the DNS is working:

```bash
# Test ping
ping shelfinity.com

# Test HTTP access
curl -I http://shelfinity.com:3000

# Test in browser
# Open: http://shelfinity.com:3000
```

## 🔐 Keycloak Configuration with Local DNS

When using `shelfinity.com`, update your Keycloak client settings:

1. Access Keycloak Admin Console: http://shelfinity.com:8080
2. Go to Clients → shelfinity-frontend → Settings
3. Update **Valid Redirect URIs**:
   ```
   http://shelfinity.com:3000/*
   ```
4. Update **Web Origins**:
   ```
   http://shelfinity.com:3000
   ```

## 🗑️ Removing Local DNS

To remove the local DNS entries:

### Linux/macOS
```bash
sudo sed -i '/shelfinity.com/d' /etc/hosts
```

### Windows
1. Open Notepad as Administrator
2. Open `C:\Windows\System32\drivers\etc\hosts`
3. Remove the lines containing `shelfinity.com`
4. Save the file

## 🚨 Troubleshooting

### DNS Not Working
```bash
# Check if entry exists in hosts file
grep shelfinity.com /etc/hosts

# Flush DNS cache (Linux)
sudo systemctl restart systemd-resolved

# Flush DNS cache (macOS)
sudo dscacheutil -flushcache

# Flush DNS cache (Windows)
ipconfig /flushdns
```

### Port Already in Use
```bash
# Check what's using the ports
sudo netstat -tulpn | grep :3000
sudo netstat -tulpn | grep :8080
sudo netstat -tulpn | grep :9080

# Kill processes if needed
sudo kill -9 <PID>
```

### Docker Issues
```bash
# Check Docker status
docker info

# Restart Docker
sudo systemctl restart docker

# Clean up containers
docker-compose down
docker system prune -f
```

## 🔄 Development Workflow

### Daily Development
```bash
# Start with localhost (faster)
./start.sh

# Or start with custom domain
sudo ./start-with-dns.sh
```

### Testing Production-like Environment
```bash
# Use custom domain for production-like testing
sudo ./start-with-dns.sh
```

## 📝 Environment Variables

The system automatically detects the domain and sets appropriate environment variables:

- `REACT_APP_API_URL`: Set to the appropriate backend URL
- `REACT_APP_KEYCLOAK_URL`: Set to the appropriate Keycloak URL

## 🎯 Best Practices

1. **Use localhost for development**: Faster startup, no sudo required
2. **Use shelfinity.com for testing**: More realistic environment
3. **Keep both options available**: Flexibility for different scenarios
4. **Document your setup**: Share configuration with team members

## 🔒 Security Notes

- Local DNS entries only affect your machine
- No external DNS resolution is involved
- Safe for development and testing
- Remove entries when not needed

## 📞 Support

If you encounter issues:

1. Check the troubleshooting section above
2. Verify Docker and Docker Compose are working
3. Check the logs: `docker-compose logs -f`
4. Ensure ports are not in use by other applications

---

**Happy coding with Shelfinity! 🚀**
