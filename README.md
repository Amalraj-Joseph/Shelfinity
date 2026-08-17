# Shelfinity - Library Management System

[![CI](https://github.com/Shadow-Codex/Shelfinity/actions/workflows/ci.yml/badge.svg)](https://github.com/Shadow-Codex/Shelfinity/actions/workflows/ci.yml)

A modern, full-stack library management system built with Jakarta EE 10, React 18, and PostgreSQL.

📖 **[Public documentation site](https://shadow-codex.github.io/Shelfinity/)** — getting started, architecture, business rules, and the full API reference.

## 🚀 Quick Start

Get the system running in 5 minutes:

```bash
# 1. Clone the repository
git clone <repository-url>
cd Shelfinity

# 2. Start all services with Docker Compose
docker-compose -f docker/docker-compose.yml up -d

# 3. Wait for services to initialize (~2 minutes)
# Access the application at http://localhost:3000
```

For detailed setup instructions, see [QUICKSTART.md](QUICKSTART.md).

---

## ✨ Features

### Core Functionality
- ✅ **User Management**: Registration, authentication, and role-based access control
- ✅ **Book Management**: CRUD operations, search, and availability tracking
- ✅ **Borrow/Return System**: Request-based workflow with admin approval
- ✅ **Queue Management**: Admin panel for processing requests

### Advanced Features (New!)
- ✅ **Email Notifications**: Automated notifications for all library events
- ✅ **Book Reservations**: Reserve unavailable books with automatic notifications
- ✅ **Overdue Tracking**: Automated daily checks with email reminders
- ✅ **Advanced Reports**: Analytics on book popularity, trends, and user activity
- ✅ **Library Statistics**: Real-time dashboard metrics

---

## 🏗️ Architecture

### Technology Stack

**Backend**
- Jakarta EE 10 (JAX-RS, JPA, CDI)
- Open Liberty 24.0.0.1
- PostgreSQL 15
- MicroProfile (JWT, OpenAPI, Health)
- Jakarta Mail for email notifications

**Frontend**
- React 18
- React Router for navigation
- Modern CSS with responsive design

**Infrastructure**
- Docker & Docker Compose
- Keycloak for OAuth 2.0/OpenID Connect
- Nginx for frontend serving

### System Components

```
┌─────────────────┐
│   React App     │ (Port 3000)
│   (Frontend)    │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│   Keycloak      │ (Port 8080)
│ (Auth Server)   │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  Open Liberty   │ (Port 9080)
│   (Backend)     │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│   PostgreSQL    │ (Port 5432)
│   (Database)    │
└─────────────────┘
```

---

## 📚 Documentation

- **[QUICKSTART.md](QUICKSTART.md)** - Get started in 5 minutes
- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** - Deploy to any environment
- **[docs/COMPLETION_REPORT.md](docs/COMPLETION_REPORT.md)** - Comprehensive deployment guide
- **[docs/NEW_FEATURES_SUMMARY.md](docs/NEW_FEATURES_SUMMARY.md)** - Detailed feature documentation
- **[docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md)** - Current implementation status
- **[docs/api/README.md](docs/api/README.md)** - API documentation

---

## 🔑 Default Credentials

### Admin User
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: Administrator

### Regular User
- **Username**: `john.doe`
- **Password**: `john123`
- **Role**: User

> ⚠️ **Security Note**: Change these credentials in production!

---

## 🌐 API Endpoints

### Authentication
- `POST /auth/login` - Login and get user info
- `GET /auth/me` - Get current user profile

### Books
- `GET /books` - List all books
- `POST /books` - Create new book (admin)
- `GET /books/{id}` - Get book details
- `PUT /books/{id}` - Update book (admin)
- `DELETE /books/{id}` - Delete book (admin)
- `GET /books/search?query={query}` - Search books

### Queue Management
- `GET /queues` - Get all queue items (admin)
- `POST /queues` - Create queue item
- `PUT /queues/{id}/status` - Update queue item status (admin)
- `GET /queues/my` - Get user's queue items

### Reservations (New!)
- `POST /reservations` - Create reservation
- `GET /reservations` - Get all reservations (admin)
- `GET /reservations/my` - Get user's reservations
- `DELETE /reservations/{id}` - Cancel reservation

### Overdue Tracking (New!)
- `GET /overdue` - Get all overdue items (admin)
- `GET /overdue/my` - Get user's overdue items
- `GET /overdue/stats` - Get overdue statistics (admin)

### Reports (New!)
- `GET /reports/book-popularity` - Most borrowed books (admin)
- `GET /reports/borrowing-trends` - Borrowing trends (admin)
- `GET /reports/user-activity` - User activity (admin)
- `GET /reports/statistics` - Library statistics (admin)
- `GET /reports/author-distribution` - Author distribution (admin)

### Email Configuration (New!)
- `GET /email-config` - Get email configuration (admin)
- `POST /email-config` - Create email configuration (admin)
- `PUT /email-config/{id}` - Update configuration (admin)
- `POST /email-config/test` - Send test email (admin)

For complete API documentation, visit: `http://localhost:9080/openapi/ui/`

---

## 🗄️ Database Schema

### Core Tables
- **users** - User accounts and profiles
- **books** - Book catalog with availability tracking
- **queue_items** - Borrow/return requests with approval workflow

### New Tables
- **reservations** - Book reservation system
- **email_config** - SMTP configuration for notifications

### Sample Data
The system includes 15 pre-loaded books for immediate testing:
- Classic literature (Pride and Prejudice, Moby Dick, etc.)
- Fantasy (Harry Potter, Lord of the Rings, etc.)
- Dystopian fiction (1984, Brave New World, etc.)
- Science fiction (Dune, Foundation, etc.)

---

## 📧 Email Configuration

### Setup SMTP
1. Access admin panel at `http://localhost:3000`
2. Navigate to Email Configuration
3. Enter your SMTP settings:
   - Host: `smtp.gmail.com` (for Gmail)
   - Port: `587` (TLS) or `465` (SSL)
   - Username: Your email
   - Password: App-specific password
4. Test the configuration
5. Activate it

### Supported Email Notifications
- User registration confirmation
- Borrow request approval/rejection
- Return confirmation
- Overdue reminders (daily at 9 AM)
- Book reservation notifications
- Profile updates
- Admin alerts

---

## 🔧 Development

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local development)
- Node.js 18+ (for frontend development)
- Maven 3.8+ (for backend development)

### Local Development Setup

**Backend**
```bash
cd backend
mvn clean package
# Deploy to Open Liberty
```

**Frontend**
```bash
cd frontend
npm install
npm start
```

### Running Tests
```bash
# Backend tests
cd backend
mvn test

# Frontend tests
cd frontend
npm test
```

---

## 📊 Monitoring & Health Checks

### Health Endpoints
- **Liveness**: `http://localhost:9080/health/live`
- **Readiness**: `http://localhost:9080/health/ready`

### Metrics
- **Application Metrics**: `http://localhost:9080/metrics`
- **OpenAPI Spec**: `http://localhost:9080/openapi`

---

## 🐛 Troubleshooting

### Common Issues

**Services not starting**
```bash
# Check logs
docker-compose -f docker/docker-compose.yml logs

# Restart services
docker-compose -f docker/docker-compose.yml restart
```

**Database connection issues**
```bash
# Verify PostgreSQL is running
docker-compose -f docker/docker-compose.yml ps postgres

# Check database logs
docker-compose -f docker/docker-compose.yml logs postgres
```

**Keycloak authentication issues**
- Ensure Keycloak is fully initialized (takes ~1-2 minutes)
- Verify realm configuration at `http://localhost:8080`
- Check that users exist in the Keycloak admin console

For more troubleshooting tips, see [docs/COMPLETION_REPORT.md](docs/COMPLETION_REPORT.md).

---

## 🚀 Deployment

### Quick Deploy to Production

See **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** for complete deployment instructions including:
- Environment variable configuration
- Cloud deployment (AWS, GCP, Azure, Kubernetes)
- SSL/TLS setup
- Security best practices
- Monitoring and logging

### Production Checklist
- [ ] Change default credentials
- [ ] Configure production database
- [ ] Set up SSL/TLS certificates
- [ ] Configure production SMTP server
- [ ] Set up monitoring and logging
- [ ] Configure backup strategy
- [ ] Review security settings
- [ ] Set up CI/CD pipeline
- [ ] Update Keycloak realm for production URLs
- [ ] Configure environment variables (see `.env.example`)

### Environment Variables
Key environment variables to configure (see `.env.example` for complete list):
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` - Database connection
- `OIDC_ISSUER` - Keycloak issuer URL
- `FRONTEND_URL` - Frontend URL for CORS
- `REACT_APP_KEYCLOAK_URL` - Keycloak URL accessible from browser

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Workflow
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write/update tests
5. Submit a pull request

---

## 📞 Support

For issues, questions, or contributions:
- Create an issue in the repository
- Check existing documentation in the `docs/` folder
- Review the API documentation at `/openapi/ui/`

---

## 🎯 Roadmap

### Completed ✅
- Core library management features
- User authentication and authorization
- Email notification system
- Book reservation system
- Overdue tracking with automated reminders
- Advanced reporting and analytics

### In Progress 🚧
- Frontend components for new features
- Bulk book upload UI
- End-to-end testing

### Planned 📋
- Mobile application
- Fine management system
- Multi-library support
- Advanced search with filters
- Book recommendations
- Reading history and statistics

---

## 📈 Project Status

**Current Version**: 1.0.0  
**Status**: Backend Complete, Frontend Pending  
**Last Updated**: 2026-03-21

### Feature Completion
- ✅ Backend API: 100%
- ✅ Database Schema: 100%
- ✅ Authentication: 100%
- ✅ Email System: 100%
- ✅ Reservations: 100%
- ✅ Overdue Tracking: 100%
- ✅ Reports: 100%
- ⏳ Frontend UI: 60%
- ⏳ Testing: 40%
- ⏳ Documentation: 90%

---

**Made with ❤️ for libraries everywhere**
