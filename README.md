# Shelfinity - Modern Library Management System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)

A modern, full-stack library management system built with Jakarta EE, React, and Docker. Shelfinity provides a comprehensive solution for managing books, users, and borrowing requests with a beautiful, responsive interface.

## 🚀 Features

- **Modern UI/UX**: Ghost black & white theme with responsive design
- **User Management**: Registration, authentication, and role-based access control
- **Book Management**: Add, edit, and manage library books
- **Request System**: Borrow and return requests with admin approval workflow
- **Admin Panel**: Comprehensive dashboard for library administrators
- **API-First Design**: RESTful API with OpenAPI documentation
- **Containerized**: Full Docker support for easy deployment
- **Identity Management**: Keycloak integration for enterprise-grade authentication

## 🏗️ Architecture

### System Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Shelfinity Library Management System              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐          │
│  │   Frontend      │    │    Backend      │    │   PostgreSQL    │          │
│  │   (React 18)    │◄──►│  (Jakarta EE)   │◄──►│   Database      │          │
│  │   Port: 3000    │    │   Port: 9080    │    │   Port: 5432    │          │
│  │   Nginx         │    │  Open Liberty   │    │   JPA/Hibernate │          │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘          │
│          │                       │                       │                  │
│          │                       │                       │                  │
│          └───────────────────────┼───────────────────────┘                  │
│                                  │                                          │
│                     ┌─────────────────┐                                     │
│                     │    Keycloak     │                                     │
│                     │  (Identity)     │                                     │
│                     │   Port: 8080    │                                     │
│                     │   OAuth 2.0     │                                     │
│                     │   JWT Tokens    │                                     │
│                     └─────────────────┘                                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Component Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Frontend Layer                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   Login     │  │  Dashboard  │  │  BookList   │  │ AdminPanel  │         │
│  │ Component   │  │ Component   │  │ Component   │  │ Component   │         │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘         │
│         │                │                │                │                │
│         └────────────────┼────────────────┼────────────────┘                │
│                          │                                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                    React App (Single Page Application)              │    │
│  │                    Ghost Black & White Theme                        │    │
│  │                    Responsive Design                                │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Backend Layer                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   Users     │  │   Books     │  │   Queues    │  │   Health    │         │
│  │ Resource    │  │ Resource    │  │ Resource    │  │ Resource    │         │
│  │ (JAX-RS)    │  │ (JAX-RS)    │  │ (JAX-RS)    │  │ (JAX-RS)    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘         │
│         │                │                │                │                │
│         └────────────────┼────────────────┼────────────────┘                │
│                          │                                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                    Jakarta EE 10 Application                        │    │
│  │                    Open Liberty Server                              │    │
│  │                    JPA/Hibernate ORM                                │    │
│  │                    JWT Authentication                               │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Data Layer                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   Users     │  │   Books     │  │   Queues    │  │  Keycloak   │         │
│  │   Table     │  │   Table     │  │   Table     │  │   Database  │         │
│  │             │  │             │  │             │  │             │         │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘         │
│         │                │                │                │                │
│         └────────────────┼────────────────┼────────────────┘                │
│                          │                                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                    PostgreSQL Database                              │    │
│  │                    - shelfinity database (app data)                 │    │
│  │                    - keycloak database (auth data)                  │    │
│  │                    - ACID compliance                                │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Data Flow

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   User      │───►│  Frontend   │───►│   Backend   │───►│  Database   │
│  Browser    │    │   React     │    │ Jakarta EE  │    │ PostgreSQL  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │                   │
       │                   │                   │                   │
       ▼                   ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Keycloak  │◄───│   JWT       │◄───│  Auth       │◄───│   User      │
│  Identity   │    │  Tokens     │    │  Service    │    │  Sessions   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

### Security Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Security Layer                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   OAuth 2.0 │  │   JWT       │  │   Role-     │  │   CORS      │         │
│  │   Protocol  │  │  Tokens     │  │   Based     │  │  Policy     │         │
│  │             │  │             │  │   Access    │  │             │         │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘         │
│         │                │                │                │                │
│         └────────────────┼────────────────┼────────────────┘                │
│                          │                                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                    Keycloak Identity Provider                       │    │
│  │                    - User Authentication                            │    │
│  │                    - Session Management                             │    │
│  │                    - Token Validation                               │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Docker Environment                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐          │
│  │   Frontend      │    │    Backend      │    │   PostgreSQL    │          │
│  │   Container     │    │   Container     │    │   Container     │          │
│  │   Port: 3000    │    │   Port: 9080    │    │   Port: 5432    │          │
│  │   Nginx         │    │  Open Liberty   │    │   Database      │          │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘          │
│          │                       │                       │                  │
│          │                       │                       │                  │
│          └───────────────────────┼───────────────────────┘                  │
│                                  │                                          │
│                     ┌─────────────────┐                                     │
│                     │   Keycloak      │                                     │
│                     │   Container     │                                     │
│                     │   Port: 8080    │                                     │
│                     └─────────────────┘                                     │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                    Docker Compose Network                           │    │
│  │                    - Internal communication                         │    │
│  │                    - Health checks                                  │    │
│  │                    - Volume persistence                             │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 📁 Project Structure

```
Shelfinity/
├── backend/                 # Jakarta EE 10 Backend (Open Liberty)
│   ├── src/
│   │   ├── main/java/com/shelfinity/
│   │   │   ├── users/         # User management
│   │   │   ├── books/         # Book management
│   │   │   ├── queues/        # Queue management
│   │   │   ├── email/         # Email configuration
│   │   │   └── security/      # JWT utilities
│   │   └── resources/
│   │       └── META-INF/
│   │           └── persistence.xml
│   ├── pom.xml
│   ├── Dockerfile
│   └── server.xml
├── frontend/               # React 18 Frontend
│   ├── public/
│   ├── src/
│   │   ├── components/        # React components
│   │   ├── App.js
│   │   └── index.js
│   ├── package.json
│   ├── Dockerfile
│   └── nginx.conf
├── docker/                 # Docker Compose configurations
│   ├── docker-compose.yml
│   ├── docker-compose-simple.yml
│   ├── docker-compose-shelfinity.yml
│   └── init-db.sql
├── scripts/                # Utility scripts
│   ├── start.sh
│   ├── build-and-start.sh
│   ├── start-with-dns.sh
│   └── setup-local-dns.sh
├── docs/                   # Documentation
│   ├── api/               # API documentation
│   │   ├── api.yaml
│   │   ├── api.html
│   │   ├── Architecture.md
│   │   ├── As_is.md
│   │   ├── Flow_chart.md
│   │   ├── README.md
│   │   ├── SRS.md
│   │   └── User_stories.md
│   ├── guides/            # Setup and configuration guides
│   │   ├── KEYCLOAK_SETUP.md
│   │   └── LOCAL_DNS_GUIDE.md
│   └── PROJECT_STATUS.md  # Project status and roadmap
├── .github/               # GitHub workflows and templates
├── .vscode/               # VS Code configuration
├── .gitignore            # Git ignore rules
├── LICENSE.txt           # MIT License
└── README.md             # This file
```

## 🛠️ Technology Stack

### Backend
- **Java 21** with Jakarta EE 10
- **Open Liberty** application server
- **PostgreSQL** database
- **JPA/Hibernate** for data persistence
- **JWT** for token-based authentication
- **Keycloak** for identity management

### Frontend
- **React 18** with modern hooks
- **CSS3** with custom ghost theme
- **Nginx** for serving static files
- **Responsive design** for all devices

### Infrastructure
- **Docker** and **Docker Compose**
- **PostgreSQL** for data storage
- **Keycloak** for authentication
- **Health checks** and monitoring

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose
- Git

### 1. Clone the Repository
```bash
git clone https://github.com/Shadow-Codex/shelfinity.git
cd shelfinity
```

### 2. Start the Application
```bash
# Start all services
./scripts/start.sh

# Or build and start (recommended for first run)
./scripts/build-and-start.sh
```

### 3. Access the Application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:9080/api
- **Keycloak Admin**: http://localhost:8080 (admin/admin)
- **API Documentation**: http://localhost:3000/docs

## 📚 API Documentation

The API documentation is available at `docs/api/` and includes:
- OpenAPI 3.0 specification
- Interactive Swagger UI
- Complete endpoint documentation
- Request/response schemas

## 🔧 Configuration

### Environment Variables
Key environment variables can be configured in the Docker Compose files:

```yaml
# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=shelfinity
DB_USER=shelfinity
DB_PASSWORD=shelfinity

# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KC_REALM=shelfinity
KC_CLIENT_ID=shelfinity-frontend
```

### Docker Compose Options
- `docker/docker-compose.yml` - Full stack with Keycloak
- `docker/docker-compose-simple.yml` - Backend + Frontend only
- `docker/docker-compose-shelfinity.yml` - Production-ready setup

## 📖 Documentation

- **[API Documentation](docs/api/)** - Complete API reference
- **[Setup Guides](docs/guides/)** - Configuration and deployment guides
- **[Keycloak Setup](docs/guides/KEYCLOAK_SETUP.md)** - Identity provider configuration
- **[Local DNS Guide](docs/guides/LOCAL_DNS_GUIDE.md)** - Local domain setup

## 🛠️ Development

### Backend Development
```bash
cd backend
mvn clean package
```

### Frontend Development
```bash
cd frontend
npm install
npm start
```

### Running Tests
```bash
# Backend tests
cd backend && mvn test

# Frontend tests
cd frontend && npm test
```

## 🐳 Docker Commands

```bash
# Start services
cd docker && docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Rebuild and start
docker-compose up -d --build
```

## 📋 Project Status

- ✅ **Core Features**: User management, book management, request system
- ✅ **UI/UX**: Modern ghost theme, responsive design
- ✅ **Backend API**: RESTful endpoints with JWT authentication
- ✅ **Database**: PostgreSQL with JPA/Hibernate
- ✅ **Containerization**: Full Docker support
- ✅ **Documentation**: API docs and setup guides
- 🔄 **Keycloak Integration**: Basic setup complete, advanced features in progress
- 🔄 **Testing**: Unit tests in progress
- 🔄 **CI/CD**: Pipeline setup in progress

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

## 🙏 Acknowledgments

- [Open Liberty](https://openliberty.io/) for the application server
- [Keycloak](https://www.keycloak.org/) for identity management
- [React](https://reactjs.org/) for the frontend framework
- [PostgreSQL](https://www.postgresql.org/) for the database

## 📞 Support

For support and questions:
- Create an issue in the GitHub repository
- Check the [documentation](docs/) for guides and FAQs
- Review the [API documentation](docs/api/) for technical details

---

**Shelfinity** - Modern Library Management System  
*Built with ❤️ using Jakarta EE, React, and Docker*





