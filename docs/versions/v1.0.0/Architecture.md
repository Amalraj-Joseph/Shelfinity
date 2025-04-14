# System Architecture Document : Library Management System (LMS) - Jakarta EE Backend

## 1. Introduction
### 1.1 Purpose

This document defines the system architecture for the Library Management System (LMS) Backend, developed using Jakarta EE. The purpose of this document is to provide an in-depth overview of the design and structure of the backend system, detailing how various components and services interact to handle critical functionalities such as:

- **User Management:** User authentication, authorization, and role-based access control.
- **Book Cataloging:** Managing book information, including adding, updating, and deleting books in the system.
- **Borrowing Workflows:** Handling the lifecycle of book borrowing, including requests, reservations, and returns.
- **Admin Approval Queues:** Admin approval processes for book transactions that require manual intervention or validation.
- **Asynchronous Email Notifications:** Sending email notifications related to book borrowing, reminders, and other system activities via an SMTP server.

This document will serve as a reference for developers to understand the backend system’s components, technologies, data flow, and integrations, ensuring that all stakeholders are aligned in their understanding of the system.

--- 

### 1.2 Scope

This document is limited to the backend services of the Library Management System (LMS) and does not cover the frontend or mobile interfaces, which are being developed separately. Specifically, the scope includes:

- **RESTful APIs:** Exposed for interaction with frontend and third-party applications.
- **Business Workflows:** Logical processes like user registration, book borrowing, return, and approval queues.
- **Data Persistence:** Interaction with a PostgreSQL relational database to store and manage data such as user information, books, transactions, and system logs.
- **Messaging System:** Integration with Apache Kafka to facilitate event-driven communication between system components, ensuring data consistency and real-time processing.
- **Email Notifications:** Integration with an SMTP server to send system notifications such as borrowing confirmations, reminders, and administrative alerts.

The scope does not extend to system components such as the **user-facing frontend** or **mobile application**. These components will interact with the backend via the provided APIs but are not part of the system design covered in this document.

--- 

### 1.3 Target Audience

The intended audience for this document includes:

- **Backend Developers and Architects:** Individuals responsible for the implementation, integration, and maintenance of the backend system.
- **DevOps and Site Reliability Engineers (SRE):** Engineers responsible for deploying, monitoring, and maintaining the backend infrastructure.
- **Quality Assurance (QA) Engineers and Testers:** Individuals responsible for validating the functionality, performance, and security of the backend system.
- **System Integrators:** External teams or contractors integrating third-party systems with the backend.
- **Project Stakeholders:** Business analysts, project managers, and others with an interest in understanding the backend system architecture from a high-level perspective.

--- 

### 1.4 Document Organization

- Section 2: Overview and goals
- Section 3: Architectural strategy
- Section 4: High-level component architecture
- Section 5: Layered design
- Section 6: Module responsibilities
- Section 7: Data and communication flow
- Section 8: Database schema
- Section 9: Security strategies
- Section 10: Scalability and performance
- Section 11: Deployment details
- Section 12: Directory structure
- Section 13: Non-functional goals
- Section 14: Future scope
- Section 15: Appendices

--- 

## 2. System Overview
### 2.1 System Objectives

The **Library Management System (LMS)** Backend is designed to support core library operations in an efficient, reliable, and scalable manner. It serves as the central engine behind the LMS ecosystem, handling all server-side logic, data processing, and integrations with external services. The key objectives of the system are as follows:

**i. User Registration and Profile Management**

- Enable new users to register using email and password or institutional credentials.
- Support secure login and logout operations with session or token-based authentication.
- Allow users to view and update personal details such as contact information, notification preferences, and account settings.
- Implement role-based access control to distinguish between regular users, librarians, and administrators.

--- 

**ii. Book Cataloging and Discovery**

- Provide administrative features to add, update, or remove books from the catalog, including metadata like title, author, ISBN, category, and availability.
- Allow users to browse or search for books using various filters such as title, author, genre, and availability.
- Support tagging, categorization, and dynamic sorting to enhance user experience.

--- 

**iii. Borrow and Return Workflow**

- Allow users to submit borrowing requests for available books.
- Route borrowing requests to administrators or librarians for approval when necessary.
- Track borrowed items, enforce borrowing limits, and manage return due dates.
- Enable users to initiate return requests and allow admins to process and close the return.

--- 

**iv. Reservation and Overdue Tracking**

- Support reservation of books that are currently checked out or unavailable.
- Maintain a queue system for reservations based on priority and request timestamp.
- Track overdue books and flag items that exceed return deadlines.
- Generate reports or alerts for overdue items to both users and admins.

--- 

**v. Email Notification System**

- Send automated email notifications for key user actions and system events, including:
  - Confirmation of registration and profile updates.
  - Acknowledgment and status updates for borrow and return requests.
  - Reminders for upcoming due dates and overdue items.
  - Admin alerts for pending approvals or exceptions.
- Utilize an SMTP server for reliable and asynchronous email delivery.

--- 

### 2.2 Key Features

The Library Management System (LMS) backend incorporates a variety of features designed to ensure a robust, maintainable, and scalable architecture. The following are the key features that define the system's core capabilities:

**i. RESTful API Interface**

- The backend exposes all functionality through a well-defined set of RESTful APIs built using Jakarta EE standards.
- APIs follow best practices in terms of resource naming, HTTP method usage, and status code conventions.
- Endpoints are organized by domain (e.g., `/users`, `/books`, `/borrow`, `/admin`) to ensure clarity and separation of concerns.
- JSON is used as the standard data format for requests and responses.
- API documentation is generated automatically using tools like OpenAPI (Swagger) for easier developer consumption and integration.

--- 

**ii. Role-Based Access Control (RBAC)**

- The system supports two primary roles: Admin and User.
- Access to sensitive or administrative functionality (e.g., approving borrow requests, managing the catalog) is restricted to Admin users.
- Users can only access APIs and features relevant to their permissions (e.g., viewing books, submitting borrow requests).
- Security is enforced at both the API layer (via interceptors and filters) and the service layer to prevent unauthorized access.
- Authentication and authorization are implemented using token-based mechanisms (e.g., JWT), with optional support for OAuth2 in future versions.

--- 

**iii. Admin Queue Management**

- Borrow and return workflows that require administrative intervention are managed via a queue-based system.
- Admins can view pending approval requests, filter them by status or user, and take appropriate actions (approve, reject, escalate).
- Each action is logged and tied to audit trails for compliance and traceability.
- Queues are optimized for concurrency and responsiveness, enabling smooth handling of high request volumes.

--- 

**iv. Kafka-Based Asynchronous Email Processing**

- The system decouples email notification logic from transactional operations using Apache Kafka.
- Events such as user registration, borrow approval, and overdue alerts are published to Kafka topics.
- A dedicated Email Notification Service subscribes to relevant topics and processes email requests asynchronously.
- This approach ensures faster response times for users and improved scalability by offloading non-critical tasks.
- Email content is templated, customizable, and localized to support multiple formats and languages.

--- 

**v. Modular Service Separation**

- The backend is organized into distinct, loosely coupled modules that follow the Separation of Concerns (SoC) principle.
- Each module handles a specific domain (e.g., User Service, Book Service, Borrowing Service, Notification Service).
- Services communicate via well-defined interfaces, making the system easier to test, maintain, and extend.
- This modular design allows teams to develop and deploy individual components independently when needed.
- Future scalability is supported by this architecture, with the possibility of migrating to a microservices model.

--- 

### 2.3 Users and Roles

The LMS backend defines two primary user roles—User and Admin—with clearly separated access privileges. Role-Based Access Control (RBAC) ensures that each user type can only perform actions appropriate to their role.

**i. User**

Represents a registered library member. Users can interact with the system to search for books, manage their borrowings, and update their personal account information.

**Capabilities:**

- **Register and Log In:** Create a new account and securely authenticate using login credentials.
- **Search and Browse Books:** Use filters such as title, author, genre, or availability to explore the catalog.
- **Borrow Books:** Submit requests to borrow available books.
- **Return Books:** Initiate returns and view borrowing history.
- **Reserve Books:** Reserve books that are currently unavailable and join the waitlist.
- **Track Requests:** Monitor the status of borrowing, return, and reservation requests.
- **Update Profile:** Edit personal details such as name, email, contact number, and notification preferences.
- **Change Password:** Update their account password securely through a dedicated API endpoint.

--- 

**ii. Admin**

Admins have elevated access for managing and maintaining the overall library system. This role is intended for librarians or system operators.

**Capabilities:**

- **Approve/Decline Requests:** Review and take action on borrow, return, and reservation requests.
- **Manage Books:** Add, update, or remove books from the catalog and manage book metadata.
- **Admin Queue Management:** View and process pending user requests through a centralized admin dashboard.
- **Configure SMTP Settings:** Set or update email server configuration for outbound notifications.
- **Generate Reports:** Produce various operational reports such as overdue summaries, borrowing statistics, and system usage metrics.

--- 

### 2.4 Business Context

The Library Management System (LMS) backend is developed as a strategic replacement for an outdated, manual ledger-based process historically used for managing library operations. The legacy system involved handwritten records, physical ledgers, and manual coordination, which introduced inefficiencies, inconsistencies, and a lack of visibility across the system.

The new LMS backend addresses these challenges by introducing a **digitized, centralized, and scalable solution** that modernizes core library functions such as user management, book cataloging, borrowing workflows, and communication.

**Key Business Drivers:**

- **Efficiency:** Automates routine tasks such as request tracking, overdue monitoring, and email notifications, reducing manual effort and processing time.
- **Scalability:** Designed to accommodate growing user bases and book collections without performance degradation.
- **Maintainability:** Built using modular architecture and modern frameworks (Jakarta EE, PostgreSQL, Kafka), enabling easier future enhancements and support.
- **Auditability:** Provides better traceability and compliance with digital logs and user activity history.
- **Future Integration:** Lays the foundation for seamless integration with future UI clients (web, desktop) and mobile applications via RESTful APIs.

This transformation empowers library administrators to manage resources more effectively and offers users a smoother, more transparent library experience. It aligns with long-term digital transformation goals and sets the stage for future innovation and service expansion.

---

## 3. Architectural Approach
### 3.1 Design Principles

The LMS backend architecture is grounded in modern, robust software engineering principles. These principles ensure the system is scalable, maintainable, and aligned with enterprise-grade backend design patterns.

---

**🔹 Modularity**

- Each functional domain — **User**, **Book**, **Borrow/Return Request**, **Admin Workflow**, and **Notification** — is developed as a logically separate module.
- Code organization and deployment strategies encourage loose coupling and high cohesion.
- This modular design simplifies independent development, testing, and potential microservice extraction in the future.

<img width="976" alt="Screenshot 2025-04-14 at 3 07 44 PM" src="https://github.com/user-attachments/assets/5e182db8-f5c3-4132-9432-2429ac357b74" />

---

**🔹 Separation of Concerns**

- The codebase is structured into distinct layers:
  - **API Layer:** Handles HTTP requests, input validation, and error mapping.
  - **Service Layer:** Contains business logic and orchestration.
  - **Persistence Layer:** Responsible for database interactions via JPA or other persistence frameworks.
  - **Integration Layer:** Manages external interactions such as Kafka and SMTP.
- This clean separation ensures better testability, traceability, and development velocity.

<img width="763" alt="Screenshot 2025-04-14 at 3 11 32 PM" src="https://github.com/user-attachments/assets/1f10d0c4-4eb5-4827-8ab5-0837f626853b" />

---

**🔹 Asynchronous Processing**

- Tasks that are non-critical to immediate user interactions (e.g., sending emails) are offloaded to **Apache Kafka**.
- Improves response times and ensures that temporary failures in downstream systems (like SMTP) do not impact core operations.
- Enables retry strategies, dead-letter queues, and observability in message delivery pipelines.

--- 

**🔹 Stateless Services**

- All service endpoints are designed to be **stateless**, making them suitable for horizontal scaling.
- Any required user context or session information is passed via request metadata (e.g., JWT).
- Ensures high availability and ease of load balancing in containerized/cloud-native environments.

--- 

**🔹 Configurable and Secure**

- Uses **MicroProfile Config** to externalize environment-specific settings (e.g., database URLs, Kafka topics, SMTP host).
- Implements robust **authentication and authorization** mechanisms:
- **JWT tokens** for secure, stateless user sessions.
- **Role-based access control (RBAC)** enforced at the API and business logic levels.
- Sensitive data like passwords and secrets are encrypted and securely stored.

---

### 3.2 Architectural Style

The LMS backend is structured using a **Layered Architecture** pattern with support for **event-driven communication** and **RESTful service exposure**. This architectural choice ensures a clear separation of responsibilities, promotes maintainability, and allows for scalable, asynchronous interactions where appropriate.

**🔹 Layered Architecture**

The system follows a classical **n-tier layered architecture**. Each layer encapsulates a specific concern and interacts only with adjacent layers.

<img width="735" alt="Screenshot 2025-04-14 at 3 21 29 PM" src="https://github.com/user-attachments/assets/d4acd094-a72e-451a-8aa0-d4e7dbdf6247" />

**i. Presentation Layer:**

- RESTful APIs exposed using Jakarta REST.
- Handles incoming HTTP requests, validation, and response formatting.

**ii. Application Layer:**

- Coordinates interactions between services, enforcing high-level workflow rules (e.g., borrowing policies).

**iii. Service Layer:**

- Contains reusable business logic—e.g., eligibility checks, book availability rules.

**iv. Data Access Layer:**

- Uses JPA for ORM-based access to the PostgreSQL database.
- Handles persistence, querying, and transaction boundaries.

**Integration Layer:**

- Publishes and consumes Kafka events (e.g., notification triggers).
- Sends emails via SMTP using Jakarta Mail.

---

**🔹 Event-Driven Communication**

The system uses **Apache Kafka** for asynchronous, decoupled communication, especially for non-critical tasks like sending notifications.

<img width="767" alt="Screenshot 2025-04-14 at 3 24 54 PM" src="https://github.com/user-attachments/assets/c2d55c98-60f8-4f00-b14c-ec397db81750" />

- This design ensures that operations like email notifications do not block or slow down synchronous REST interactions.
- Kafka topics are also used for future extensibility—e.g., analytics or audit trails.

---

**🔹 REST-Based Communication**

- All client-facing interactions are exposed through **RESTful endpoints**.
- Standard HTTP methods (`GET`, `POST`, `PUT`, `DELETE`) are used for CRUD operations.
- Uses **JSON** as the payload format.
- All endpoints are secured with JWT-based authentication and role-based authorization.
- Versioning (`/api/v1/...`) supports long-term backward compatibility.

<img width="893" alt="Screenshot 2025-04-14 at 3 33 28 PM" src="https://github.com/user-attachments/assets/58509535-8f3d-4614-9ed8-a9589fdc2196" />


This architectural style provides a balance between structure and flexibility, allowing the LMS to scale horizontally, integrate cleanly with modern frontend/mobile apps, and evolve over time with minimal disruption.

---

### 3.3 Deployment Model

The LMS backend is containerized and built for portability and consistency across development, test, and production environments. The deployment model leverages Docker, a Liberty runtime, and a set of supporting services for messaging and persistence.

---

**🔹 Containerized Deployment**

- **Docker** is used to package the backend service and supporting components.
- Each component runs as an isolated container, making it easy to manage, test, and scale independently.
- A `docker-compose.yml` file or Kubernetes manifests may be used to orchestrate local deployments.

---

**🔹 Runtime Environment**

- The Jakarta EE application is deployed on **Open Liberty** (or WebSphere Liberty), a lightweight and cloud-optimized runtime.
- Configuration is externalized via environment variables and `MicroProfile Config`.

---

**🔹 Event Queuing: Apache Kafka**

- Kafka runs as a separate container and acts as the event backbone for asynchronous communication.
- LMS publishes events (like `email.notification`) to Kafka, which are later consumed by background workers.

---

**🔹 Data Persistence: PostgreSQL**

- All structured, transactional data (users, books, borrow logs, etc.) is stored in a PostgreSQL instance.
- PostgreSQL runs in its own container and uses volumes for persistent data storage.

---

**🔹 Optional Caching: Redis**

- Redis is considered for future caching use cases like frequently searched books or session/token storage.
- It is not required in the current MVP but may be added as the system scales.

---

🔸 Deployment Topology 

<img width="891" alt="Screenshot 2025-04-14 at 3 39 59 PM" src="https://github.com/user-attachments/assets/48fcdaf8-6215-4717-842d-c99b0f752491" />

---

**🔹 Key Benefits of the Model**

- **Portable:** Runs identically across dev, staging, and prod environments.
- **Extensible:** Easily add new services like Redis or Elasticsearch.
- **Dev-Friendly:** Rapid local setup using Docker Compose.
- **Cloud-Ready:** Can be adapted for Kubernetes/OpenShift deployment later.

---

## 4. High-Level Architecture
### 4.1 Component Overview

The LMS backend is composed of several logically distinct modules. Each module encapsulates a specific domain responsibility and communicates with others either synchronously via REST or asynchronously via Kafka.

---

**🔹 Component Descriptions**

**i. ✅ User Management**

- Handles registration, authentication (e.g., JWT), profile updates, and password changes.
- Manages role-based access control (admin vs. user).
- Exposes endpoints like `/api/v1/users`, `/api/v1/auth/login`.

**ii. 📚 Book Management**

- Responsible for cataloging, updating, and searching books.
- Supports filtering by title, author, genre, availability.
- Admins can add/update/delete books.

**iii. 🔁 Request Management (Borrow/Return)**

- Processes borrow and return requests submitted by users.
- Validates user eligibility and book availability.
- Sends request to admin queue for approval.
- Tracks due dates and return status.

**iv. 📅 Reservation & Overdue Management**

- Allows users to reserve unavailable books.
- Handles queueing logic and automatic reservation notification once available.
- Periodically checks for overdue books and marks them accordingly.

**v. ✉️ Email Notification Service**

- Subscribes to Kafka `email.notification` events.
- Sends asynchronous emails (e.g., approval status, due date reminders) via SMTP.
- Decoupled from core API layer for scalability.

**vi. 📊 Reporting Module**

- Accessible to admins only.
- Generates reports on:
  - Borrowed books
  - Active reservations
  - Overdue returns
  - User activity logs
 
---

**🔸 High-Level Component Diagram**

<img width="938" alt="Screenshot 2025-04-14 at 3 48 34 PM" src="https://github.com/user-attachments/assets/89e0a311-7c84-461d-abd0-7eabd3d41adb" />

---

**🔹 Communication Patterns**

- **REST:** Between clients and API modules.
- **Kafka Events:** Between business services (Request, Reservation) and Email Service.
- **JPA:** All modules persist data through the shared PostgreSQL database.
- **MicroProfile Config:** Manages external configs for each module.

---

### 4.2 Module Responsibilities

Each module in the LMS backend follows a consistent structure:

- **Exposes REST APIs** to external clients (e.g., web or mobile apps).
- **Contains business** logic encapsulated in service classes.
- **Persists data** using JPA/Hibernate to a PostgreSQL database.
- **Integrates with messaging** layers such as Kafka for asynchronous processing (e.g., notifications).

---

**🔹 Common Internal Structure of a Module**

Each module is organized into these internal layers:

- **API Layer:** REST endpoints (Jakarta JAX-RS)
- **Service Layer:** Business logic
- **Persistence Layer:** JPA entities and repositories
- **Integration Layer:** Kafka producers/consumers, external services (SMTP)

---

**🔹 Kafka Integration**

- **Producers:** Modules like Request Management and Reservation publish events (e.g., `BookRequestApproved`, `OverdueReminder`) to Kafka topics.
- **Consumers:** A separate **Email Worker Service** subscribes to these topics and sends emails asynchronously using SMTP.

---

**🔸 Module Interaction Diagram**

<img width="936" alt="Screenshot 2025-04-14 at 4 03 12 PM" src="https://github.com/user-attachments/assets/d19d167e-4fc5-4dd6-ac02-2d08c5435d19" />

---

**🔹 Benefits of This Architecture**

- **Decoupling:** Core services don’t wait on email sending.
- **Scalability:** Email worker can scale independently.
- **Resilience:** Failures in the email system don't impact main APIs.
- **Reusability:** Module structure is uniform, promoting consistent development patterns.

---

### 4.3 Kafka Event Communication

Kafka is used to **decouple time-independent operations**, particularly email notifications. This ensures that the user experience is not delayed by background tasks and that message delivery can be retried or audited independently.

**🔹 Topic Naming Conventions**

```
lms.<module>.<event_type>
```
**Examples:**

| **Kafka Topic Name**             | **Description**                                      |
|----------------------------------|------------------------------------------------------|
| `lms.request.approved`           | A borrow request has been approved                   |
| `lms.request.rejected`           | A borrow/return request has been rejected            |
| `lms.reservation.available`      | Reserved book is now available                       |
| `lms.overdue.reminder`           | A user has overdue books                             |
| `lms.user.registration`          | Welcome email after user registration                |
| `lms.user.password_changed`      | Confirmation email after password change             |


**🔹 Email Event Schema**

Each Kafka message is a JSON payload that the Email Notification Service can consume and interpret. Here's a general schema format:

```
{
  "to": "user@example.com",
  "subject": "Book Request Approved",
  "template": "request_approved",
  "data": {
    "userName": "Jane Doe",
    "bookTitle": "Clean Code",
    "dueDate": "2025-05-01"
  }
}
```

**🔸 Schema Fields**

| **Field**   | **Description**                                                   |
|-------------|-------------------------------------------------------------------|
| `to`        | Recipient email address                                           |
| `subject`   | Email subject line                                                |
| `template`  | Template name used by the email service                           |
| `data`      | Template parameters (varies based on the template selected)       |


**🔹 Template Examples**

| **Template Name**     | **Used For**                        | **Required Data Keys**                   |
|------------------------|--------------------------------------|-------------------------------------------|
| `request_approved`     | Book borrow approval                | `userName`, `bookTitle`, `dueDate`        |
| `request_rejected`     | Book request rejection              | `userName`, `bookTitle`, `reason`         |
| `reservation_ready`    | Reserved book is now available      | `userName`, `bookTitle`                   |
| `overdue_notice`       | Overdue return reminder             | `userName`, `bookTitle`, `daysOverdue`    |
| `welcome_email`        | New user registration               | `userName`                                |
| `password_changed`     | Password updated notification       | `userName`                                |



### 4.4 Technology Stack

The LMS backend is built with a robust set of modern technologies, enabling modularity, scalability, and ease of deployment.

**🔹 Application Framework**

| **Technology** | **Purpose** |
|----------------|-------------|
| **Jakarta EE** | Core framework for enterprise Java application development. Key specifications used include:<br>→ **CDI** – Contexts and Dependency Injection for loose coupling<br>→ **JAX-RS** – RESTful API definition<br>→ **JPA** – ORM for relational database interaction |

**🔹 Data Storage**

| **Technology**  | **Purpose** |
|-----------------|-------------|
| **PostgreSQL**  | Relational database for persistent storage of users, books, transactions, etc. |

**🔹 Messaging & Asynchronous Processing**

| **Technology**   | **Purpose**                                                        |
|------------------|--------------------------------------------------------------------|
| **Apache Kafka** | Asynchronous event broker used for inter-module communication and email triggering |

**🔹 Notification Service**

| **Technology** | **Purpose**                                                      |
|----------------|------------------------------------------------------------------|
| **SMTP**       | Standard email protocol used for sending notifications via the Email Worker |

**🔹 Runtime & Packaging**

| **Technology** | **Purpose**                                                  |
|----------------|--------------------------------------------------------------|
| **Liberty**    | Lightweight, cloud-optimized Jakarta EE runtime (production use) |
| **Payara**     | Optional Jakarta EE-compatible alternative (mainly for local dev/test) |
| **Maven**      | Build automation, dependency management                      |

**🔹 Containerization**

| **Technology**     | **Purpose**                                              |
|--------------------|----------------------------------------------------------|
| **Docker**         | Packaging applications and services                      |
| **Docker Compose** | Define and manage multi-container environments (e.g., app + DB + Kafka) |

---


## 5. Architecture Layers
### 5.1 Presentation Layer

The **Presentation Layer** is responsible for handling HTTP requests and serving the API endpoints. It is the entry point for communication between external clients (e.g., web or mobile apps) and the backend system.

This layer primarily consists of:

**i. REST APIs:**

- **JAX-RS** (Jakarta API for RESTful Web Services) is used to expose endpoints to external consumers.
- Each module (e.g., User Management, Book Management) exposes relevant REST APIs to enable interactions like user registration, borrowing books, reserving books, etc.

**ii. Input Validation**:

- Ensures incoming requests contain valid data (e.g., JSON schema validation).
- The validation layer is also responsible for sanitizing inputs to prevent security risks (e.g., SQL injection, XSS).

**iii. API Controllers:**

- Defines the RESTful services exposed by each module.
- Routes requests to the corresponding service layer for business logic processing.

**iv. Security:**

- Handles authentication and authorization.
- Uses mechanisms like JWT (JSON Web Tokens) or OAuth2 to verify user identity and enforce role-based access control.

---

**🔸 Presentation Layer Diagram**

<img width="970" alt="Screenshot 2025-04-14 at 4 36 21 PM" src="https://github.com/user-attachments/assets/3a9b7725-b731-4052-b97e-98d7b36f1649" />

---

**🔹 Key Responsibilities**

**i. Routing:**

- The **API Gateway** or individual controllers route incoming HTTP requests to the appropriate backend service (e.g., User Management, Book Management).

**ii. Security & Authentication:**

- The **Security Layer** ensures that each API request is authenticated (e.g., by validating JWT tokens or using OAuth2 tokens).
- Role-based access control ensures that only users with proper roles (e.g., admin or user) can access specific API endpoints.

**iii. Input Validation:**

- Incoming data is validated to ensure it meets business rules before passing it on to the service layer. This prevents invalid data from propagating to the core business logic.

---

**🔹 Presentation Layer Flow**

When a client makes an HTTP request, the flow of data goes as follows:

- 1. The request is routed by the API Gateway or controller to the corresponding module (e.g., User Management).
- 2. The request is validated, and if authenticated, it proceeds to the **Service Layer** for business logic.
- 3. The response is then generated and sent back to the client, potentially including validation feedback or data updates (e.g., book borrowing confirmation).

---

### 5.2 Service Layer

The Service Layer contains the core business logic of the LMS backend. It acts as the central coordinator between the REST API layer above and the data/integration layers below. This layer ensures business rules are enforced consistently and workflows are executed in a modular, transactional, and scalable manner.

**🔹 Responsibilities**

- Implements domain-specific logic for modules such as user management, book borrowing, and reservations
- Orchestrates operations across repositories and external services (e.g., email)
- Publishes domain events to Kafka topics for asynchronous processing
- Ensures transactional consistency for multi-step operations
- Does not handle HTTP-level concerns or direct database queries

**🔹 Components**

- `UserService`: Manages user registration, profile updates, and password changes
- `BookService`: Handles book creation, updates, and availability checks
- `BorrowService`: Processes borrow and return requests, checks borrowing limits
- `ReservationService`: Handles reservations, overdue logic, and queue management
- `NotificationService`: Publishes Kafka events and delegates email notification tasks
- `KafkaPublisher`: Utility for sending structured messages to Kafka topics

**🔹 Design Notes**

- Stateless by design to support horizontal scalability
- Annotated with `@Transactional` for managing consistency across operations
- Interfaces are defined to allow easy mocking and testing
- Designed to evolve easily by adding support for additional workflows or integrations

**🔸 Diagram: Service Layer Interactions**

<img width="748" alt="Screenshot 2025-04-14 at 4 51 44 PM" src="https://github.com/user-attachments/assets/17b6e91c-819b-4226-844f-e0d552b6f9f2" />

---

### 5.3 Messaging Layer

The Messaging Layer enables asynchronous communication using Kafka. It allows decoupling of non-blocking tasks like email delivery from synchronous user interactions, enhancing responsiveness and scalability of the system.

**🔹 Responsibilities**

- Publishes domain events from core services (e.g., user registration, borrow actions)
- Triggers background processes such as email sending through consumers
- Decouples workflows to improve modularity and reduce direct dependencies
- Supports event logging and monitoring for observability

**🔹 Kafka Topics**

- `user-events`: Triggered on user-related actions like registration or password change
- `book-events`: Triggered on book-related workflows such as borrow, return, or overdue updates

**🔹 Kafka Consumers**

- Email Worker Service subscribes to both user-events and book-events
- Processes event payloads to identify notification type
- Generates and sends emails via SMTP based on mapped templates

**🔸 Messaging Flow (Producer → Kafka → Consumer)**

<img width="685" alt="Screenshot 2025-04-14 at 4 58 10 PM" src="https://github.com/user-attachments/assets/06e3765c-347d-4388-9ae6-451330093de6" />

---

### 5.4 Data Access Layer

The Data Access Layer is responsible for abstracting database operations through a consistent and type-safe interface. It uses JPA with Hibernate for Object-Relational Mapping (ORM), making it easier to manage relational data through Java entities.

**🔹 Responsibilities**

- Encapsulates direct interactions with the PostgreSQL database
- Maps Java entities to relational tables using JPA annotations
- Provides repository classes for each domain module (e.g., UserRepository, BookRepository)
- Supports querying using JPA Criteria API, JPQL, or named queries
- Keeps business logic isolated from persistence logic

**🔹 Technology and Tools**

- **JPA (Java Persistence API)** — standard ORM specification
- **Hibernate** — JPA implementation provider
- **PostgreSQL** — relational database backend
- **EntityManager** — used for transaction-scoped database operations

**🔹 Example Structure**
- `UserRepository`: Handles CRUD and custom queries for users
- `BookRepository`: Manages persistence of book catalog entries
- `BorrowRepository`: Tracks borrow and return records
- `ReservationRepository`: Manages hold queues and overdue flags

**🔸 Data Flow Diagram**

<img width="592" alt="Screenshot 2025-04-14 at 5 06 37 PM" src="https://github.com/user-attachments/assets/bcddf4e7-186a-4f79-8636-fa3d19c23b32" />

---

### 5.6 Integration Layer

The Integration Layer handles all interactions with external systems. It ensures reliable communication with infrastructure components such as messaging systems and email servers, enabling seamless coordination beyond the core application logic.

**🔹 Responsibilities**

- Manages Kafka producer and consumer configurations
- Handles publishing and consuming events from Kafka topics
- Sends email notifications via configured SMTP server
- Isolates third-party and infrastructure-level integrations from business logic
- Provides configuration endpoints for runtime environment settings (e.g., SMTP credentials, Kafka broker details)

**🔹 External Systems**

- **Kafka** — for asynchronous message publishing and consumption
- **SMTP** — for sending transactional and status notification emails
- **MicroProfile Config** — to inject external system configurations at runtime

**🔹 Email Notification Integration**

- Uses JavaMail API or equivalent Jakarta Mail API
- Email templates mapped to Kafka events
- Supports retry and error logging for failed deliveries

**🔸 Integration Flow**

<img width="492" alt="Screenshot 2025-04-14 at 5 10 19 PM" src="https://github.com/user-attachments/assets/5bcd8e15-120d-40d6-b112-97b27227372e" />



**🔸 Overall Layered Architecture**

<img width="788" alt="Screenshot 2025-04-14 at 5 39 20 PM" src="https://github.com/user-attachments/assets/438f1ab9-f730-4630-959b-8b053bf6f4af" />



**🔸 Full System Architecture**

<img width="1728" alt="Screenshot 2025-04-14 at 5 37 57 PM" src="https://github.com/user-attachments/assets/fdabf917-de0c-48cf-ad67-1211e897d858" />

---


## 6. Key Modules and Responsibilities

### 6.1 User Management

Responsible for managing the entire user lifecycle including registration, approval, credential management, and profile updates.

**🔹 Registration Workflow**

📝 User Onboarding

- A new user registers by providing details like name, email, password, and role preference.
- The submitted data is validated at the API level.

🔐 Secure Credential Handling

- Passwords are hashed using a strong one-way hashing algorithm (e.g., bcrypt or PBKDF2).
- No plaintext passwords are stored or logged.

📤 Kafka Event Publishing

- Once a user is successfully registered, a `USER_REGISTERED` event is published to the `user-events` Kafka topic.
- This enables asynchronous downstream actions like sending welcome emails.

⏳ Approval Required

- Registered users are marked as `PENDING_APPROVAL`.
- They cannot log in until explicitly approved by an admin.

---

**🔹 Admin Approval Flow**

🔍 Pending User Review

- Admins can retrieve a list of pending users using a secured endpoint.
- Filtering options are available based on registration date or email.

✅ Approval/Decline Actions

- Admins may either approve or reject a user.
- On approval:
  - The user status is updated to `ACTIVE`.
  - A `USER_APPROVED` event is published.
  - An approval email is sent via the Kafka email worker.
- On rejection:
  - The user status is updated to `REJECTED`.
  - No access to the system is granted.

---

**🔹 Password Management**

🔄 Password Update

- Authenticated users can update their passwords from the profile section.
- The API verifies the old password before updating.
- New password is hashed and persisted.

🆘 Forgot Password (Future Scope)

- A separate endpoint will allow users to initiate password reset.
- This will trigger an email containing a one-time reset link (via Kafka).

📤 Event Emission

- A `PASSWORD_CHANGED` event is published after successful update.

---

**🔹 Profile Updates**

🧾 Editable Fields

- Users can modify non-sensitive profile fields such as full name, contact number, and display preferences.

🛡️ Audit and Validation

- Changes are validated and optionally audited for traceability.

🔄 Real-Time Update

- Changes take immediate effect unless flagged for manual approval (e.g., email changes in future scope).

---

**🔹 Kafka Events Summary**

| Event Name        | Trigger                    | Topic        |
|-------------------|-----------------------------|--------------|
| `USER_REGISTERED`   | After successful registration | `user-events`  |
| `USER_APPROVED`     | On admin approval             | `user-events`  |
| `PASSWORD_CHANGED`  | After password update         | `user-events`  |

---

**🔸 Technical Flow**

<img width="548" alt="Screenshot 2025-04-14 at 5 57 55 PM" src="https://github.com/user-attachments/assets/e0d76ca4-545c-4259-8cff-3c2f991d0479" />

---


### 6.2 Book Management

This module is responsible for maintaining the book catalog, enabling both individual and bulk management of books along with search and filter capabilities for end users.

**🔹 CRUD Operations**

➕ Add New Books

- Admins can add books through a RESTful endpoint with fields like title, author, ISBN, category, language, and availability status.

📝 Update Book Info

- Fields such as title, author, summary, or availability can be modified.
- The `updated_at` timestamp is auto-managed.

❌ Delete Books

- Books can be removed by ID. Deletion is soft (marked as inactive) to preserve history.

🔍 Get Book Details

- Books can be retrieved individually by ID or in paginated lists.
- Includes optional status check (`AVAILABLE`, `RESERVED`, `BORROWED`).

---

**🔹 Bulk Upload via CSV**

📂 CSV Upload API

- Admins can upload a CSV file through a designated endpoint.

📊 File Format

- Required headers: `title`, `author`, `isbn`, `category`, language, year

⚙️ Processing Logic

- Validates each row.
- Skips or logs errors for malformed entries.
- Saves valid records in a single transaction for efficiency.

🔔 Post-Processing Notification

- A success or error report can be generated and emailed asynchronously (future scope).

---

**🔹 Metadata-based Filtering**

🔎 Search Functionality

- Users can query books using combinations of:
  - Title
  - Author
  - Category
  - Language
  - Availability status

⏱️ Pagination and Sorting

- All list endpoints support pagination, sorting (e.g., by title or publication year), and keyword-based search.

🧠 Future Scope

- Advanced filtering (e.g., fuzzy search, tag-based classification, ML recommendations)

---

**🔹 Kafka Events Summary**

| Event Name    | Trigger              | Topic        |
|---------------|----------------------|--------------|
| `BOOK_ADDED`    | On adding new book   | `book-events`  |
| `BOOK_UPDATED`  | On update            | `book-events`  |
| `BOOK_DELETED`  | On soft-delete       | `book-events`  |

---

<img width="644" alt="Screenshot 2025-04-14 at 6 13 34 PM" src="https://github.com/user-attachments/assets/3c647833-66b6-41bf-91b2-39af7b2e04ee" />

---

### 6.3 Request Management

This module is responsible for managing the full lifecycle of borrow and return requests. It enforces business rules and exposes queues for admin approvals.

**🔹 Borrow Request Lifecycle**

📝 Request Submission

- Users submit a borrow request via API.
- Checks book availability and user borrow limits.
- Sets request status as `PENDING_APPROVAL`.

🔄 Admin Approval Workflow

- Admins can view a queue of pending requests.
- Approve: Status changes to `BORROWED`, due date is assigned.
- Reject: Status changes to `DECLINED`.

📅 Due Date Handling

- Default due date is calculated based on user type (student/staff).
- Returned books marked with actual return date.

🔄 Return Request

- Users initiate return via API.
- Admin verifies condition and finalizes the return.
- Status set to `RETURNED`.

---

**🔹 Approval Queue for Admins**

🧾 Queue API- RESTful endpoint returns pending borrow/return approvals, sorted by request time.

⚠️ Validation Checks

- Book availability.
- User’s active borrow count.
- Outstanding fines or overdue books.

🔄 Status Transitions

| Action          | From              | To              |
|-----------------|-------------------|------------------|
| Submit Borrow   | N/A                 | `PENDING_APPROVAL` |
| Approve         | `PENDING_APPROVAL`  | `BORROWED`         |
| Reject          | `PENDING_APPROVAL`  | `DECLINED`         |
| Submit Return   | `BORROWED`          | `RETURN_PENDING`   |
| Verify Return   | `RETURN_PENDING`    | `RETURNED`         |

---

**🔹 Kafka Events Summary**

| Event Name        | Trigger                     | Topic          |
|-------------------|-----------------------------|----------------|
| `BORROW_REQUESTED`  | Borrow request submitted    | `request-events` |
| `BORROW_APPROVED`   | Admin approval              | `request-events` |
| `RETURN_REQUESTED`  | User initiates return       | `request-events` |
| `RETURN_CONFIRMED`  | Admin marks as returned     | `request-events` |

---

**🔸 Technical Flow**

<img width="675" alt="Screenshot 2025-04-14 at 6 44 41 PM" src="https://github.com/user-attachments/assets/4ff45f42-85c2-4739-9ad9-fab49cfd2a14" />

---

### 6.4 Reservation & Overdue

This module handles scenarios where books are not immediately available and ensures timely returns through overdue tracking and notifications.

**🔹 Book Reservation**

📚 Reserve Unavailable Books

- If a book is currently borrowed, users may place a reservation.
- Books can have a reservation queue (FIFO).
- On return, the next in queue is notified.

🔁 Auto-Promotion

- When a book is returned, the system auto-promotes the next reservation to active status.

⏳ Reservation Expiry

- Reservations must be claimed within a configured window (e.g., 2 days), or it's passed to the next user.

---

**🔹 Overdue Tracking**

🕒 Due Date Monitoring

- Nightly job checks for overdue books.
- Flags entries as `OVERDUE`.

📧 Notification Logic

- Sends reminder email to users with overdue items.
- Escalates if return is delayed beyond grace period.

---

**🔹 Kafka Events Summary**

| Event Name           | Trigger                  | Topic              |
|----------------------|---------------------------|---------------------|
| `BOOK_RESERVED`        | User reserves book        | `reservation-events`  |
| `RESERVATION_EXPIRED`  | Reservation timeout       | `reservation-events`  |
| `BOOK_OVERDUE`         | Scheduled overdue check   | `overdue-events`      |

---

**🔸 Technical Flow**

<img width="760" alt="Screenshot 2025-04-14 at 6 49 53 PM" src="https://github.com/user-attachments/assets/e074a225-a7b7-46d6-901c-b42e488b8e4a" />


### 6.5 Email Notifications

This module handles all outbound email communication for the system. It ensures that user-triggered or system-triggered events result in timely and reliable notifications using Kafka for asynchronous delivery.

**🔹 Notification Types**

✉️ Registration & Approval

- Sent after successful registration and upon admin approval.
- Templates include user-specific info like username, next steps, etc.

📚 Borrow and Return Events

- Triggered after borrow approval or return confirmation.
- Includes due date, return confirmation, or any remarks.

⏰ Overdue Alerts

- Periodic reminders for users with overdue books.
- Can be configured for escalation levels.

📦 Reservation Ready Notification

- Sent when a reserved book becomes available.
- Instructs user on how to claim the book.

---

**🔹 Architecture**

📤 Kafka-Driven Delivery

- Email service subscribes to multiple topics:
  - user-events
  - request-events
  - reservation-events
  - overdue-events
- Converts event payloads into template-based emails.

⚙️ SMTP Integration

- Uses external SMTP configuration (admin-managed).
- Email credentials, host, and port are loaded via MicroProfile Config.

📄 Templating System

- Templates are stored in resource files and populated dynamically.
- Supports plain text and HTML formats.

---

**🔹 Kafka Consumer Logic**

| Topic              | Event Type         | Email Triggered                        |
|--------------------|--------------------|----------------------------------------|
| `user-events`        | `USER_REGISTERED`    | Welcome email                          |
|                      | `USER_APPROVED`      | Approval confirmation                  |
| `request-events`     | `BORROW_APPROVED`    | Borrow success notification            |
|                      | `RETURN_CONFIRMED`   | Return acknowledgment                  |
| `reservation-events` | `BOOK_RESERVED`      | Reservation placed confirmation        |
|                      | `RESERVATION_READY`  | Reserved book available alert          |
| `overdue-events`     | `BOOK_OVERDUE`       | Overdue warning                        |

---

<img width="664" alt="Screenshot 2025-04-14 at 6 57 55 PM" src="https://github.com/user-attachments/assets/1f1323c4-d1a8-4187-b18b-7a4d5882766b" />

---

### 6.6 Reporting

The Reporting module provides insights into system usage, book inventory trends, and user activities. It is intended for admins and other stakeholders who require analytics to support decision-making and operational improvements.

**🔹 Report Types**

📈 Activity Reports

- Tracks user activities: logins, borrow/return requests, reservations.
- Useful for auditing and behavior analysis.

📚 Book Availability Statistics

- Shows current availability of books in the catalog.
- Categorized by genre, author, popularity.

🔄 Usage Trends

- Historical borrowing patterns and high-demand periods.
- Helps in acquisition planning and library resource optimization.

🧾 Admin Logs

- Captures admin actions: approvals, role changes, system settings.
- Aids in accountability and auditing

---

**🔹 Data Sources**

🗃️ Database Tables

- Aggregates data from user, book, request, and reservation tables.
- Uses SQL views and queries for performance.

📨 Kafka Topics (Optional)

- Can optionally subscribe to events like `user-events` and `request-events` for near-real-time dashboards.

---

**🔹 Export Options**

📤 CSV and JSON Downloads

- Admin can export reports for offline analysis.
- Filtered by time window, user type, or category.

🖥️ Admin Dashboard Integration

- Endpoints exposed via REST APIs.
- Can be consumed by a frontend dashboard or reporting UI.

---

**🔸 Technical Flow**

<img width="459" alt="Screenshot 2025-04-14 at 7 02 07 PM" src="https://github.com/user-attachments/assets/e71f2a12-0bbc-4029-a6b4-2cfc0c81659f" />

---

### 6.7 Admin Configuration

This module provides administrative control over system-wide settings such as email server configuration and role-based access management. It ensures that the system remains secure, maintainable, and adaptable to organizational policies.

**🔹 SMTP Server Configuration**

⚙️ Manage Outbound Email Settings

- Admins can configure SMTP host, port, username, password, and security protocol (e.g., SSL/TLS).
- These settings are loaded via MicroProfile Config or updated via secure admin APIs.

🔐 Secure Credential Storage

- Credentials are encrypted and stored securely in the backend config store.
- Supports secret rotation without redeploying the application.

🧪 Test Connectivity

- A test-email endpoint is available to validate the configuration before activation.

---

**🔹 Role and Permission Management**

👤 Role Assignment

- Supports two main roles:
  - `ADMIN` – Full system access
  - `USER` – Restricted to personal and borrow-related actions
- Roles are stored in the database and linked to user accounts.

🛂 Authorization Rules

- APIs are secured using annotations (`@RolesAllowed`) on resource endpoints.
- Role checks are enforced at both the API and service levels.

🔄 Dynamic Role Updates

- Role changes take effect immediately after admin action—no need to restart or re-authenticate.

---

**🔹 Configuration Interfaces**

🖥️ Admin-only REST APIs

- Secure endpoints for updating SMTP and roles.
- Access is logged and requires authentication.

📁 Fallback to Config Files

- Default SMTP settings can be bootstrapped from `microprofile-config.properties` during deployment.

**🔸 Technical Flow**

<img width="550" alt="Screenshot 2025-04-14 at 8 32 15 PM" src="https://github.com/user-attachments/assets/da685faa-2f87-41fb-81a5-3f9d9bca2dad" />


## 7. Communication and Data Flow

This section explains how data moves through the system during both synchronous API requests and asynchronous event-driven processes. It also includes sequence diagrams for key operations.

### 7.1 Synchronous API Flows

This section outlines the **real-time**, request-response communication patterns used in the system. These synchronous flows ensure immediate feedback to the user and typically involve input validation, business rule enforcement, and database persistence.


**🔹 i. User APIs**

These APIs are accessible to regular users after authentication. They manage the full lifecycle of a user’s interaction with their account and book services.

🔐 POST `/register`

- Registers a new user.
- Validates required fields like email and password.
- Creates a user in pending status.
- Emits a **USER_REGISTERED** event to Kafka for async email.

🔐 POST `/login`

- Authenticates credentials and returns a JWT token on success.
- Validates email/password against stored credentials.

🔐 POST `/password/change`

- Authenticated users can change their password.
- Verifies current password, saves the new one (hashed).
- Triggers Kafka event for password change notification.

🔍 GET `/profile`

- Returns authenticated user’s profile details.
- No persistence or Kafka event involved.

✏️ PUT `/profile`

- Updates user information like name, contact, etc.
- Validates input and updates the user record.
- Emits a `PROFILE_UPDATED` event for email confirmation.

---

**🔹 ii. Admin APIs**

These are accessible only to users with admin privileges. Admins handle system management, approval workflows, and configuration.

📥 GET `/requests`

- Fetches the list of pending borrow/return/reserve requests.
- Results are scoped to actionable items for the admin.

✅ POST `/requests/approve`

- Approves a specific user request (e.g., borrow).
- Updates request status, marks book as borrowed.
- Emits a `BORROW_APPROVED` or `RESERVE_APPROVED` event.

❌ POST `/requests/decline`

- Declines a user request.
- Updates request status and sends notification.
- Emits a `BORROW_DECLINED` or `RESERVE_DECLINED` event.

📂 POST `/books/upload`

- Allows bulk book uploads via CSV file.
- Parses file and persists book metadata in batch.

➕ POST `/books`

- Adds a new book to the catalog.
- Validates metadata and stores in DB.

🛠️ PUT `/books/{id}`

- Updates existing book details.
- Used for metadata changes like genre, author, etc.

🗑️ DELETE `/books/{id}`

- Deletes a book entry.
- Requires checks for active borrow or reservation.

📊 GET `/reports`

- Generates reports on system usage, book availability, overdue metrics, etc.
- Data is aggregated from multiple modules.

📧 POST `/email/config`

- Adds or updates the SMTP configuration used by the email service.
- Persisted to be referenced by async worker.

---

**🔹 iii. Book APIs**

These APIs support the user-facing interactions around browsing and requesting books.

🔍 GET `/books`

- Lists all books, with support for filters like genre, author, availability.
- No Kafka interaction.

📚 POST `/borrow`

- User submits a borrow request.
- Creates a new request with status pending.
- Emits `BORROW_REQUESTED` event.

🔁 POST `/return`

- User indicates they are returning a borrowed book.
- Updates book availability and request status.
- Emits `BOOK_RETURNED` event.

⏳ POST `/reserve`

- Allows a user to reserve a currently unavailable book.
- Adds a reservation entry to the queue.
- Emits `BOOK_RESERVED` event.

---

🔸 Technical Flow

<img width="472" alt="Screenshot 2025-04-14 at 9 38 21 PM" src="https://github.com/user-attachments/assets/9ed28739-98e1-48f7-86c1-6cac481398ea" />

---


### 7.2 Asynchronous Email Events

This section explains the asynchronous communication architecture where certain user or admin actions emit **Kafka events**, which are later consumed by a background service (EmailWorker) to send appropriate emails. This decouples the core application logic from the email delivery process.

📨 Event Emission

- Certain user or admin-triggered actions result in a domain event being published to Kafka.
- These events follow a standard format and include all metadata required to construct and send an email.

⚙️ Kafka Topics

- `user-events`: Handles events related to user registration, profile updates, password changes, etc.
- `book-events`: Covers borrow, return, reserve, and admin approval/decline events.

🧵 EmailWorker

- A dedicated background service that listens to relevant Kafka topics.
- Upon consuming an event, it constructs an email payload and interacts with the configured SMTP server to send emails.
- Operates independently from API availability.

---

**🔹 Types of Email Notifications**

Below are the different asynchronous email scenarios supported by the system:

👤 Registration Confirmation

- Sent when a user registers successfully.
- Trigger: `USER_REGISTERED`

📥 Borrow Request Acknowledgment

- Notifies the user that their borrow request was received.
- Trigger: `BORROW_REQUESTED`

✅ Borrow Request Approval

- Sent when an admin approves a borrow request.
- Trigger: `BORROW_APPROVED`

❌ Borrow Request Decline

- Sent when an admin declines a borrow request.
- Trigger: `BORROW_DECLINED`

🔁 Book Return Confirmation

- Confirms that the return was successful.
- Trigger: `BOOK_RETURNED`

⏰ Overdue Reminder

- Periodic reminders for users with overdue books.
- Trigger: scheduled job + event `BOOK_OVERDUE`

🔔 Admin Request Queue Alert

- Notifies admins of new pending requests.
- Trigger: `REQUEST_QUEUE_UPDATED`

✏️ Profile Update Notification

- Confirmation email after user profile is updated.
- Trigger: `PROFILE_UPDATED`

🔑 Password Change Notification

- Security notification after password is changed.
- Trigger: `PASSWORD_CHANGED`

🆕 User Registration Request (for Admin)

- Informs admin about a newly registered user.
- Trigger: `USER_REGISTERED`

---

**🔸 Technical Flow**

<img width="483" alt="Screenshot 2025-04-14 at 9 43 54 PM" src="https://github.com/user-attachments/assets/1202ba99-2f57-440d-9e81-ffb863d36bb4" />

---

### 7.3 Sequence Diagrams

This section illustrates end-to-end flows using sequence diagrams, capturing how data and events move between layers and systems for each major action.

**🔹 i. User Registration Flow**

- 🧾 **Action:** New user registration
- 🎯 **Goal:** Save user and notify both user and admin

**🔸 Technical Flow**

<img width="527" alt="Screenshot 2025-04-14 at 9 47 35 PM" src="https://github.com/user-attachments/assets/c6db4712-ba08-4f44-b7b0-88828477f34e" />

---

**🔹 ii. Borrow Request Flow**

- 🧾 **Action:** User requests to borrow a book
- 🎯 **Goal:** Save request, notify admin, and acknowledge user

**🔸 Technical Flow**

<img width="492" alt="Screenshot 2025-04-14 at 9 48 57 PM" src="https://github.com/user-attachments/assets/976cfdee-f699-4dea-a7cf-32dbabad8b45" />

---

**🔹 iii. Borrow Approval / Decline Flow**

- 🧾 **Action:** Admin approves or declines a borrow request
- 🎯 **Goal:** Update status and notify the user

**🔸 Technical Flow**

<img width="489" alt="Screenshot 2025-04-14 at 9 50 16 PM" src="https://github.com/user-attachments/assets/b6db7004-642e-42cd-9a36-6c08c6176097" />

---

**🔹 iv. Return Book Flow**

- 🧾 **Action:** User returns a borrowed book
- 🎯 **Goal:** Update records and confirm return to user

**🔸 Technical Flow**

<img width="527" alt="Screenshot 2025-04-14 at 9 52 45 PM" src="https://github.com/user-attachments/assets/65da0c0a-ab45-4657-b940-45e9ae0fb5b1" />

---

**🔹 v. Overdue Reminder Flow**

- 🧾 **Action:** A book is overdue
- 🎯 **Goal:** Send a reminder email to the user

**🔸 Technical Flow**

<img width="501" alt="Screenshot 2025-04-14 at 9 54 01 PM" src="https://github.com/user-attachments/assets/ef5e58a7-d3e0-43ba-9b7f-a6f006df7acc" />

---

**🔹 vi. Profile / Password Update Flow**

- 🧾 **Action:** User updates profile or password
- 🎯 **Goal:** Save changes and send confirmation

**🔸 Technical Flow**

<img width="505" alt="Screenshot 2025-04-14 at 9 56 17 PM" src="https://github.com/user-attachments/assets/9a0b4faa-e93a-4d9e-b9fe-a0cbdeac2749" />

---

**🔹 vii. Bulk Upload Books via CSV**

- 🧾 **Action:** Admin uploads a CSV with book data
- 🎯 **Goal:** Parse file and insert books into the database

**🔸 Technical Flow**

<img width="1013" alt="Screenshot 2025-04-14 at 9 58 25 PM" src="https://github.com/user-attachments/assets/4f0ef9a7-572e-4f62-9f65-9b1dfa97b2b7" />

---

**🔹 viii. Add or Update Book Entry**

- 🧾 **Action:** Admin adds or updates individual book info
- 🎯 **Goal:** Modify or insert book record

**🔸 Technical Flow**

<img width="1025" alt="Screenshot 2025-04-14 at 9 59 40 PM" src="https://github.com/user-attachments/assets/2fbb1e09-b65d-44d4-abb7-b280716d112b" />

---

**🔹 ix. Generate Reports**

- 🧾 **Action:** Admin requests activity/statistical reports
- 🎯 **Goal:** Aggregate and return relevant data

**🔸 Technical Flow**

<img width="986" alt="Screenshot 2025-04-14 at 10 00 47 PM" src="https://github.com/user-attachments/assets/545509d5-65f3-471b-8876-e404a3fcc46a" />

---

**🔹 x. Configure SMTP Server**

- 🧾 **Action:** Admin sets or updates email server
- 🎯 **Goal:** Persist configuration for use in EmailWorker

**🔸 Technical Flow**

<img width="975" alt="Screenshot 2025-04-14 at 10 02 49 PM" src="https://github.com/user-attachments/assets/c0c9e25c-46b2-4081-87de-15f951bdad60" />

---

**🔹 xi. Book Reservation Flow**

- 🧾 **Action:** User reserves an unavailable book
- 🎯 **Goal:** Save reservation and notify admins

**🔸 Technical Flow**

<img width="1014" alt="Screenshot 2025-04-14 at 10 04 42 PM" src="https://github.com/user-attachments/assets/94239469-21e7-4b5b-aff3-314c7d3439a0" />

---

### 7.4 Failure Scenarios

Failure handling is critical to ensure system reliability and user feedback. The architecture uses retry mechanisms, error logs, and fallback responses to manage failures.

**i. Email Delivery Failure**

- 📬 Trigger: EmailWorker fails to send email (SMTP timeout, auth failure, etc.)
- 🔁 Strategy: Retry with backoff → mark as permanently failed after threshold
- 📓 Log: Error stored in mail logs for debugging
- 🧑‍💻 User Impact: No email notification, but core transaction still succeeds

**🔸 Technical Flow** 

<img width="999" alt="Screenshot 2025-04-14 at 10 11 27 PM" src="https://github.com/user-attachments/assets/dc056060-dc0e-4312-9f63-9c5619b8c752" />

---

**ii. Database Timeout or Unavailability**

- 🧱 Trigger: DB query times out or DB is unreachable
- 🔁 Strategy: Retry for transient failures → return `503 Service Unavailable` if persistent
- 🔐 Protection: Connection pool limits, transaction timeouts, rollback
- 🧑‍💻 User Impact: Operation fails with a clear error message

**🔸 Technical Flow**

<img width="1006" alt="Screenshot 2025-04-14 at 10 13 17 PM" src="https://github.com/user-attachments/assets/2a9678ff-567c-443f-9d1c-87637f794a6b" />

---

### 7.5 State Transitions

State transitions are used to represent the lifecycle of requests and reservations in the system.

**i. Borrow Request Lifecycle**

- 📌 Initial: PENDING
- ✅ On admin approval: APPROVED
- ❌ On decline: DECLINED
- 📘 On book return: RETURNED
- ⚠️ On overdue: OVERDUE

**🔸 State Transition Flowchart**

<img width="852" alt="Screenshot 2025-04-14 at 10 16 59 PM" src="https://github.com/user-attachments/assets/5998c297-27d7-4ba2-87e7-c344ae0fbdeb" />

---

**ii. Reservation Lifecycle**

- 🟢 Initial: RESERVED
- ✅ On book return: ALLOCATED
- 🔁 If expired without return: CANCELLED

**🔸 State Transition Flowchart**

<img width="789" alt="Screenshot 2025-04-14 at 10 18 51 PM" src="https://github.com/user-attachments/assets/afa8012c-8bac-478e-9d42-23acd3393540" />

---

**iii. User Registration Lifecycle**

- 🟡 Initial: REQUESTED
- ✅ On admin approval: APPROVED
- ❌ On rejection: REJECTED
- 🔒 On inactivity or issue: SUSPENDED

**🔸 State Transition Flowchart**

<img width="434" alt="Screenshot 2025-04-14 at 10 22 14 PM" src="https://github.com/user-attachments/assets/877fd573-ac89-4d7d-9e92-e7e364307703" />

---

**iv. Email Delivery Lifecycle (Internal to EmailWorker)**

- ⏳ Initial: QUEUED
- 🚀 On successful send: SENT
- 🔁 On transient failure: RETRYING
- ❌ On max retries exceeded: FAILED

**🔸 State Transition Flowchart**

<img width="609" alt="Screenshot 2025-04-14 at 10 23 47 PM" src="https://github.com/user-attachments/assets/47890611-5469-4086-97b5-73690defe2b5" />

---

**v. Password Reset Lifecycle**

- 🔑 Initial: REQUESTED
- ✅ On completion: UPDATED
- ⛔ On expiration: EXPIRED
- 🔄 On retry: RE-REQUESTED

**🔸 State Transition Flowchart**

<img width="349" alt="Screenshot 2025-04-14 at 10 29 47 PM" src="https://github.com/user-attachments/assets/b8754146-4b9b-4bc8-9583-8fce19d91c91" />

---

**vi. Book Upload Lifecycle**

- 📤 CSV file is uploaded by the admin via `/books/upload`.
- ✅ Each record is validated (columns, values, duplicates).
- 💾 Valid entries are saved to the database.
- ❌ If any critical error occurs (invalid format, DB error), upload fails entirely.
- 🔁 Retry is possible by re-uploading the corrected file.

**🔸 State Transition Flowchart**

<img width="581" alt="Screenshot 2025-04-14 at 10 31 53 PM" src="https://github.com/user-attachments/assets/4c6c1b01-abf4-4090-b1ac-4a2c3265b29f" />

---

**vii. Profile Update Lifecycle**

- 🧑 User sends request to update profile via `/profile`.
- 🔐 Server performs validations (e.g., email format, name).
- 💾 On success, the updated profile is persisted.
- ⚠️ If validation fails or a DB error occurs, user gets an error response.

**🔸 State Transition Flowchart**

<img width="788" alt="Screenshot 2025-04-14 at 10 33 58 PM" src="https://github.com/user-attachments/assets/f364fa4b-0f63-4791-a620-7a958c178405" />

---

**viii. Report Generation Lifecycle**

- 📊 Admin initiates report request via `/reports`.
- 🧵 Report is queued and processed asynchronously.
- 🏁 Upon success, report is made available for download.
- 🚫 In case of failure (e.g., DB timeout), appropriate message is returned.

**🔸 State Transition Flowchart**

<img width="497" alt="Screenshot 2025-04-14 at 10 35 31 PM" src="https://github.com/user-attachments/assets/eb6e4ec3-72ba-45d3-8e91-137562fdc340" />

---

**ix. Admin Email Config Lifecycle**

- ⚙️ Admin sets SMTP config via `/email/config`.
- 📬 System sends a test email to verify the setup.
- 🔁 Configurations can be updated anytime.
- 🚨 Errors in setup (invalid credentials, host) will show up during test email or email delivery.

**🔸 State Transition Flowchart**

<img width="526" alt="Screenshot 2025-04-14 at 10 37 16 PM" src="https://github.com/user-attachments/assets/9a1c15c4-21bc-41e5-bd2c-fef76db4da65" />

---

**🔹 State Transition Summary**

| 🔹 Lifecycle        | States                                           | Key Transitions / Notes                                                                 |
|--------------------|--------------------------------------------------|------------------------------------------------------------------------------------------|
| **User Registration**  | `[*] → Pending → Approved / Rejected`              | Admin manually approves/rejects new users                                                |
| **Borrow Request**     | `[*] → Requested → Approved / Rejected → Returned` | Admin manages approvals; user returns book                                               |
| **Reservation**        | `[*] → Reserved → Expired / Fulfilled`             | Auto-expire after time limit if not fulfilled                                            |
| **Overdue Tracking**   | `[*] → DueSoon → Overdue → Returned`               | Notifications sent as due date approaches or passes                                      |
| **Book Upload**        | `[*] → UploadQueued → Processing → Uploaded / UploadFailed` | CSV validated and persisted; retry needed on failure                             |
| **Profile Update**     | `[*] → Requested → Updated / Failed`               | User-initiated updates; may fail on validation or DB issues                              |
| **Report Generation**  | `[*] → Queued → Generating → Completed / Failed`   | Background job processes and prepares downloadable reports                               |
| **Admin Email Config** | `[*] → NotConfigured → Configured → Updated / Error` | SMTP tested during config; failures occur on bad creds or network                        |
| **User Login**         | `[*] → Authenticated / Failed`                     | User submits credentials, system checks and either grants access or denies              |

---

## 8. Database Design
### 8.1 ER Diagram

The **Entity-Relationship (ER) Diagram** is designed to represent the structure of the system’s database, where we define the entities (tables) and their relationships.

We have four main entities in this system:

- users
- books
- requests
- reservations

Here’s a breakdown of the **tables**, their **fields**, and **relationships**.

**i. users Table**

- **id:** The unique identifier for each user in the system. This is typically a primary key, usually either an auto-incremented integer or a UUID.
- **name:** The full name of the user.
- **email:** The email address of the user, which must be unique to each user to avoid duplicates.
- **status:** The current status of the user. Possible values could include `pending`, `approved`, or `rejected`. This status will indicate whether the user has been approved by an admin or is awaiting approval.

---

**ii. books Table**

- **id:** The unique identifier for each book. Like the **users** table, this is the primary key and can be an auto-incremented integer or UUID.
- **title:** The title of the book.
- **author:** The author of the book.
- **status:** The current status of the book. Possible values might be `available`, `checked-out`, `reserved`, etc. This field helps track the availability of a book in the system.

---

**iii. requests Table**

- **id:** A unique identifier for each request. This could be an auto-incremented integer or UUID.
- **user_id:** A foreign key that references the **users** table, representing the user who made the request (either to borrow or reserve a book).
- **book_id:** A foreign key that references the **books** table, representing the book that was requested.
- **type:** The type of request being made. Possible values could include `borrow` or `reserve`, indicating whether the user is borrowing the book or reserving it for future checkout.
- **status:** The current status of the request. This could be `pending`, `approved`, or `rejected`. It reflects the admin's action on the request.

---

**iv. reservations Table**

- **id:** A unique identifier for each reservation. Like the other tables, this will be a primary key.
- **user_id:** A foreign key that references the **users** table, representing the user who made the reservation.
- **book_id:** A foreign key that references the **books** table, representing the book that is reserved by the user.

---

### 8.2 Relationships

Understanding how different entities interact with each other is crucial for maintaining the integrity and consistency of data. Below are the relationships between the entities:

**i. One-to-many: users → requests**

- **Explanation:** Each **user** can have multiple **requests**, as they can request multiple books over time. However, each **request** belongs to only one **user**.
- **Foreign Key:** The requests table contains a foreign key `user_id`, referencing the `id` field in the **users** table.
- **Example:** A user can request several books, but each request is linked back to a single user.

ER Representation:
`users (1) → requests (many)`

---

**ii. One-to-many: books → requests**

- **Explanation:** Each **book** can be requested by multiple **users**. However, each **request** pertains to only one **book**.
- **Foreign Key:** The **requests** table contains a foreign key `book_id`, referencing the `id` field in the **books** table.
- **Example:** Multiple users might request the same book, but each request corresponds to one specific book.

ER Representation:
`books (1) → requests (many)`

---

**iii. Many-to-one: reservations → books/users**

- **Explanation:** The **reservations** table establishes a many-to-one relationship with both the **users** and **books** tables. This means that each **reservation** links a specific **user** and a **book**. A user can reserve multiple books, and a book can be reserved by multiple users over time.
- **Foreign Keys:** The **reservations** table contains two foreign keys: `user_id` (referencing **users**), and `book_id` (referencing **books**).
- **Example:** A user may reserve several books, but each reservation is tied to one specific user and one specific book.

ER Representation:
`users (1) → reservations (many)`
`books (1) → reservations (many)`

---

### 8.3 Constraints

Database constraints are used to maintain data integrity, enforce business rules, and ensure the relationships between tables are accurate. Below are the key constraints applied in the schema.

**i. Unique Constraints**

- **email in the users table:**
  - The **email** field is required to be unique across all users. This ensures that no two users have the same email address, preventing potential user data conflicts and ensuring correct identification.
  - **Example**: If two users try to register with the same email address, the database will reject the second entry due to the unique constraint.

- **ISBN in the books table (optional, depending on the schema's need):**
  - The **ISBN** number for each book (if included) must be unique. This ensures that each book can be uniquely identified and avoids duplication of books with the same ISBN.
  - **Example**: If a user attempts to add a book that already exists (same ISBN), the system will reject the new entry, ensuring only one record exists for that ISBN.

**ii. Foreign Keys with Cascading Updates**

- Foreign keys are used to establish and enforce a relationship between two tables. Cascading updates and deletes ensure data integrity when records are modified or deleted in the parent table.

- **users → requests:**
 - If a **user** is deleted or their **id** is updated, the corresponding **requests** that reference this user will be automatically updated or deleted based on the cascading rule.

- **books → requests:**
 - Similarly, if a **book** is deleted or its **id** is updated, the corresponding **requests** will be automatically updated or deleted.

- **users → reservations:**
 - If a **user** is deleted or their **id** is updated, the associated **reservations** will be updated or deleted according to the cascading rule.

- **books → reservations:**
 - If a **book** is deleted or its **id** is updated, the corresponding **reservations** will be automatically updated or deleted based on the cascading rule.

**Example of Cascade:**

When a **user** is deleted, any **requests** or **reservations** made by that user will also be deleted automatically. This helps maintain data integrity by preventing orphaned records that no longer correspond to valid users.


<!--

9. Security Design

9.1 Authentication

JWT-based login

Token validation middleware

9.2 Authorization

@RolesAllowed annotations

User vs Admin endpoint segregation

9.3 Secure Input & Transport

HTTPS enforced

Input sanitization and validation

10. Scalability and Performance

10.1 Stateless Services

Horizontally scalable API layer

10.2 Kafka

Decoupled email handling

Enables retry and fault-tolerance

10.3 Database Performance

PostgreSQL indexing

Query optimization for search and reporting

11. Deployment and Configuration

11.1 Local Environment

Docker Compose: Liberty, Kafka, PostgreSQL

11.2 Runtime Profiles

application-dev.yml

application-prod.yml

11.3 Config Management

MicroProfile Config

DB-based dynamic config for email settings

12. Project Directory Structure

lib-mgmt/
├── api-gateway/
├── modules/
│   ├── user/
│   ├── book/
│   ├── request/
│   ├── reservation/
│   ├── email/
│   └── report/
├── common/
├── kafka/
├── config/
├── docker/
├── docs/
└── pom.xml

13. Non-Functional Requirements

99.9% availability

Secure APIs and external integrations

Low latency for API requests (<300ms avg)

Extensible module design

14. Future Considerations

UI frontend integration (React/Vue)

Redis caching layer

Metrics/observability integration

Audit logging

I18n and template-based email rendering

15. Appendices

A. Sequence Diagrams

Registration

Borrow Request

Email Flow

B. Glossary

JWT: JSON Web Token

RBAC: Role-Based Access Control

C. API Reference

See OpenAPI spec in /docs/api.yaml

D. Kafka Events

user-events

book-events

<--
