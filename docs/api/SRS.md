# Software Requirement Specification (SRS) for Library Management System

## 📘 1. Introduction

### 📌 1.1 Purpose
This document provides a detailed Software Requirements Specification (SRS) for the Library Management Application. It defines the system's functional and non-functional requirements and provides details about the architecture, user interfaces, and external systems it will interact with. The intended audience includes developers, testers, and stakeholders.

---

### 🌐 1.2 Scope
The Library Management Application allows users to register, browse books, borrow and return books, and manage their accounts. Admins can approve or reject user registrations and book borrowing/return requests. The system will also send email notifications to users and admins when a request status changes.

This document covers the backend API and does not include UI design or implementation, which will be handled in a separate phase.

---

### 🧠 1.3 Definitions, Acronyms, and Abbreviations

| **Acronym/Term**        | **Definition/Description**                                                                                              |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------|
| **API**                 | Application Programming Interface, a set of rules that allow different software components to communicate with each other.|
| **SRS**                 | Software Requirements Specification, a document that outlines the system’s functionalities, design, and constraints.      |
| **Admin**               | A user with higher privileges, responsible for approving/denying requests, managing users, and generating reports.          |
| **User**                | A customer using the library management system to borrow, return, or reserve books, and manage their account information.    |
| **Request Queue**       | A list of pending requests (user registration, borrow/return books) waiting for admin approval or action.                  |
| **Book Borrowing**      | The process where a user requests to borrow a book from the library, subject to admin approval.                            |
| **Book Returning**      | The process where a user returns a borrowed book to the library, subject to admin review.                                  |
| **Book Reservation**    | The functionality that allows users to reserve books that are currently unavailable, to be notified when the book is available. |
| **Authentication**      | The process of verifying a user’s identity, typically through a username and password, to grant access to their account.    |
| **Authorization**       | The process of determining what actions a user is permitted to perform within the system once authenticated.              |
| **CRUD**                | Create, Read, Update, Delete — basic operations for managing user data, books, and requests.                               |
| **Overdue Alerts**      | Notifications sent to users via email to remind them to return borrowed books that are past their due date.                |
| **Book ID**             | A unique identifier assigned to each book in the system for tracking and management purposes.                              |
| **ISBN**                | International Standard Book Number, a unique identifier for books, often used for searching and categorization.            |
| **Admin Reports**       | Reports that provide insights into user activities, book availability, overdue items, and other key metrics for library management. |
| **SMTP**                | Simple Mail Transfer Protocol, the protocol used to send email notifications to users and admins.                         |
| **Session Management**  | The system’s ability to track a user’s active session, ensuring secure access and operations while the user is logged in.  |
| **UI (User Interface)** | The graphical interface through which users interact with the system, including forms, buttons, and search features.        |
| **RESTful API**         | A type of API that adheres to REST (Representational State Transfer) principles, typically used to manage interactions between the front-end and back-end systems. |
| **2FA (Two-Factor Authentication)** | A security mechanism that requires users to verify their identity through two methods: something they know (password) and something they have (e.g., a one-time code). |
| **RBAC (Role-Based Access Control)** | A security model that assigns permissions based on user roles, ensuring that only authorized users can access specific resources or perform certain actions. |
| **Password Management** | The ability for users to change and securely manage their passwords, ensuring system security and user privacy.              |
| **Book Availability**   | The status of a book in the library, indicating whether it is available for borrowing, reserved, or checked out.            |
| **Borrow Request**      | A request initiated by a user to borrow a book from the library, which must be approved by an admin.                        |
| **Return Request**      | A request initiated by a user to return a book to the library, which must be reviewed and approved by an admin.            |
| **Email Notifications** | Emails sent to users and admins to notify them of important actions, such as registration approvals, book availability, or overdue reminders. |
| **Database Schema**     | The structure that defines the organization of data within the database, including tables, relationships, and data types.   |
| **User Profile**        | The collection of a user’s personal information, including their name, email, address, and borrowing history.               |
| **ISBN Search**         | A feature that allows users to search for books by their ISBN (International Standard Book Number) to find specific titles. |
| **API Endpoint**        | A specific URL or function within an API that allows users or admins to interact with a particular feature of the system, such as requesting a book or updating their profile. |
| **Status Update**       | A change in the status of a request, such as "approved," "rejected," or "pending," often accompanied by an email notification. |
| **User Role**           | The defined level of access a user has within the system, which can include admin, user, or guest roles.                   |
| **Audit Log**           | A record of all significant actions performed by users or admins in the system, often used for security or troubleshooting. |
| **Load Balancing**      | The method of distributing incoming network traffic across multiple servers to ensure the system remains responsive and efficient under high demand. |

---

### 📚 1.4 References
| Document Title                   | Description                                                                      | Link     |
|----------------------------------|----------------------------------------------------------------------------------|----------|
| **User Manual**                      | Instructions for end users on how to use the system                             | Coming soon |
| **PostgreSQL Documentation**         | Official documentation for the PostgreSQL database                              | [Docs](https://www.postgresql.org/docs/) |
| **Java API Documentation**           | Standard Java library reference                                                 | [Docs](https://docs.oracle.com/en/java/javase/) |
| **Jakarta EE / Java EE Docs**        | Documentation for Enterprise Edition APIs (like JAX-RS, JPA, Servlet, etc.)     | [Docs](https://jakarta.ee/specifications/) |
| **Liberty Runtime Documentation**    | Guides and reference for deploying applications in Open Liberty or WebSphere Liberty | [Docs](https://openliberty.io/docs/) |
| **Payara Server Documentation**      | Documentation for running Jakarta EE apps on Payara Server                      | [Docs](https://docs.payara.fish/) |
| **Maven Documentation**              | Official documentation for building and managing Java projects with Maven       | [Docs](https://maven.apache.org/guides/) |
| **Swagger / OpenAPI Specification**  | Format for defining RESTful API contracts                                       | [Docs](https://swagger.io/docs/) |
| **Markdown Style Guide**             | Basic syntax and formatting tips for writing markdown docs                      | [Docs](https://www.markdownguide.org/basic-syntax/) |
| **SMTP Protocol Reference**          | Reference for how email is sent (for notifications)                             | [Docs](https://datatracker.ietf.org/doc/html/rfc5321) |
| **Apache Kafka Documentation**       | Official documentation for Apache Kafka, a distributed event streaming platform | [Docs](https://kafka.apache.org/documentation/) |
| **Git Versioning Workflow**          | Guide for versioning and branching documentation or source code                 | [Docs](https://nvie.com/posts/a-successful-git-branching-model/) |
| **OWASP Authentication Cheat Sheet** | Security best practices for login and password management                       | [Docs](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html) |
| **JSON Schema Specification**        | Specification for validating JSON API request and response structures           | [Docs](https://json-schema.org/learn/getting-started-step-by-step.html) |
| **Ansible Documentation**            | Official docs for automating deployments and infrastructure                     | [Docs](https://docs.ansible.com/ansible/latest/index.html) |
| **REST API Best Practices**          | Microsoft's guide on designing good RESTful APIs                                | [Docs](https://learn.microsoft.com/en-us/azure/architecture/best-practices/api-design) |

---

### 🗺️ 1.5 Overview

This document outlines:
1. The functional requirements of the system
2. Non-functional requirements (performance, security, etc.)
3. System architecture (High level)
4. External interfaces and data flows (high level)
5. Assumptions and dependencies

---

## 🧩 2. Overall Description

### 🖼️ 2.1 Product Perspective
The Library Management Application is a standalone web-based solution to manage library operations. It will interact with the PostgreSQL database for storage and will provide APIs to facilitate various operations like user registration, book borrowing, and returning.

---

### ✨ 2.2 Product Features

The Library Management System will provide the following key features:
| Feature                     | Description** |
|-----------------------------|---------------|
| **User Registration** | New users can sign up by providing personal details such as name, address, occupation, phone number, and email. |
| **User Login** | Registered users can securely log in using their email and password. |
| **Admin Queue Management** | Admins can view, approve, or reject pending user registration requests. |
| **Password Management**: | Users can securely change their password through their profile. |
| **Book Browsing and Search** | Users can browse books by category, author, or title, and apply filters such as title, author, or ISBN to refine their search. |
| **Book Borrowing and Returning** | Users can request to borrow or return books. These requests are managed and approved by admins. |
| **Book Reservations** | Users can reserve books that are currently checked out or unavailable. |
| **Overdue Book Alerts** | Users will receive automated email alerts for overdue books. |
| **Email Server Configuration** | Administrators can dynamically register and update the email server configuration through an API, allowing modifications to SMTP settings at runtime without requiring redeployment. |
| **Email Notifications** | Both users and admins will receive email notifications for relevant status changes such as registration approval or borrow request responses. |
| **Admin Reports** | Admins can generate reports showing book availability, user activity, and overdue book status. |
| **User Profile Management** | Users can view and update their profile information. |
| **Book Management (Admin only)** | Admins can add new books (individually or in bulk), update existing book information, or remove books from the system. |

---

### 👥 2.3 User Classes and Characteristics

 **i. Admin**

- **Responsibilities:** Admins have higher-level privileges within the system. They are responsible for managing user accounts, reviewing and approving/rejecting user registration requests, handling borrow/return requests for books, and managing the library’s book collection (adding, editing, or removing books).
- **Actions:** Admins can view and manage the requests queued by users, approve or decline user registrations, and manage the borrowing and returning of books. They also generate and view system reports.
- **Characteristics:** Admins are typically system administrators or staff members with a comprehensive understanding of the system. They have a higher level of access to the system's features compared to regular users.

**ii. User**

- **Responsibilities:** Users are regular patrons of the library system. They have the ability to register for an account, log in, browse books, borrow or return books, and manage their profile.
- **Actions:** Users can search for books, borrow or return books, and reserve books if needed. They will also receive notifications about overdue books and can update their personal information through their profile settings.
- **Characteristics:** Users are typically patrons of the library, including individuals who seek to browse or borrow books. They are expected to be comfortable using web-based applications but may not have deep technical knowledge of the backend operations.

---

### 🖥️ 2.4 Operating Environment

**i. Hardware**

The system is designed to be run on a variety of hardware environments. Since the backend is Java-based, it is compatible with a range of devices, including:

- **Linux:** Popular for server environments, ideal for deploying enterprise-level applications.
- **macOS:** Can be used by developers for local development and testing.
- **Windows:** Commonly used by both developers and end-users; supported for running the system in different development stages or on desktops.

**Recommended Specs:** For optimal performance, a system with at least 4GB RAM and 2 CPU cores is recommended. However, the system can scale depending on the load and infrastructure used.

**ii. Software**

- **Backend:** The system backend is designed to run on IBM Liberty, a lightweight, modular application server, offering quick startup time and high performance.
- **Database:** The system will use PostgreSQL as the relational database management system (RDBMS). PostgreSQL is known for its robustness, scalability, and compliance with ACID properties. It will store user data, book details, borrowing history, and transaction logs.
- **Frontend:** While not the focus at the moment, the system is expected to expose RESTful APIs that can be consumed by any frontend client (e.g., a web application). The API will be built using Jakarta EE for consistency and scalability.

---

### 🧾 2.5 Assumptions and Dependencies

**i. User Access**

- The system assumes that all users have internet access to interact with the library system. Internet connectivity is required for both the registration process and for browsing books, borrowing, returning, or reserving items.

- While the system does not support offline modes at this stage, it is assumed that most interactions will happen through an internet-connected device.

**ii. Email Service**

- Email notifications for actions like user registration, password resets, borrow/return request statuses, and overdue alerts will require integration with an email service (SMTP server). This is a dependency for communication between the system and its users.

- The choice of the SMTP server can vary, but options include using popular services like SendGrid, Amazon SES, or an on-premise email server. The system assumes the existence of such an email service for all notifications to function.

**iii. Database**

- The system will depend on PostgreSQL for database management. It is assumed that the database will be properly set up, configured, and secured. The PostgreSQL instance will be responsible for storing all user data, book information, transaction history, and system logs.

- The application will rely on PostgreSQL’s reliability, scalability, and support for complex queries to maintain system performance even with an expanding dataset over time.

---

## ⚙️ 3. System Features (Functional Requirements)

### 📝 3.1 User Registration
**Description:** Users can register by providing their personal details. The request goes into the admin’s approval queue.

- **Inputs:** Name, Address, Occupation, Phone Number, Email
- **Outputs:** Confirmation message (successful registration), Error message (invalid input)

**Functional Requirements:**

- Validate user input before submission.
- Store the registration as a pending request.
- Notify the admin via email when a new registration is submitted.

---

### 🔐 3.2 User Login
**Description:** Registered users can log in using their email and password.

- **Inputs:** Email, Password
- **Outputs:** Login success or failure message

**Functional Requirements:**

- Authenticate credentials against stored user data.
- Restrict access for unapproved or inactive accounts.

---

### 🔁 3.3 Password Management
**Description:** Users can securely change their password.

- **Inputs:** Old Password, New Password
- **Outputs:** Success or error message

**Functional Requirements:**

- Validate the old password before applying the change.
- Notify the user via email upon successful password change.

---

### 📥 3.4 Admin Queue Management
**Description:** Admins manage incoming requests including registrations, borrow/return actions, and reservations.

- **Inputs:** Queue of pending requests
- **Outputs:** Request status updates (approved/declined)

**Functional Requirements:**

- Admin can view and respond to all pending user actions.
- Request status is updated upon admin action.
- Users are notified via email when requests are approved or rejected.

---

### 🔍 3.5 Book Browsing and Search
**Description:** Users can explore available books and apply search filters.

- **Inputs:** Filter parameters (e.g., title, author, category, ISBN)
- **Outputs:** List of matching books

**Functional Requirements:**

- Allow browsing by category, author, or title.
- Enable filtering and searching by metadata.

---

### 📚 3.6 Book Borrowing and Returning
**Description:** Users may request to borrow or return books. Admins must approve these requests.

- **Inputs:** Book ID, User ID, Request Type
- **Outputs:** Confirmation of request submission and approval status

**Functional Requirements:**

- Users can place borrow/return requests.
- Admins review and update request status.
- Users are notified once the request is processed.

---

### 🕒 3.7 Book Reservations
**Description:** Users can reserve unavailable books.

- **Inputs:** Book ID
- **Outputs:** Reservation confirmation

**Functional Requirements:**

- Prevent reservation of available books.
- Queue reservations based on book availability.
- Notify users when a reserved book becomes available.

---

### ⏰ 3.8 Overdue Book Alerts
**Description:** Users are automatically notified of overdue books.

**Functional Requirements:**

- Detect and track overdue status based on due dates.
- Send automated email reminders to affected users.

---

### 📦 3.9 Book Management (Admin only)
**Description:** Admins can manage book inventory in the system.

- **Inputs:** Book details (title, author, ISBN, quantity, etc.)

- **Outputs:** Confirmation of book addition, update, or removal

**Functional Requirements:**

- Add new books individually or through bulk upload.
- Edit or delete existing book records.
- Validate input data before making changes.

---

### 📊 3.10 Admin Reports
**Description:** Admins can generate reports on key metrics.

- **Inputs:** Report parameters (e.g., date range, user activity, overdue status)
- **Outputs:** Generated report

**Functional Requirements:**

- Generate reports for book availability, user activity, and overdue status.
- Provide exportable/downloadable format for reports.

---

### 🙍‍♂️ 3.11 User Profile Management
**Description:** Users can view and update their profile.

- **Inputs:** Updated profile fields
- **Outputs:** Confirmation message

**Functional Requirements:**

- Allow users to update address, phone number, and other non-sensitive details.
- Require email verification for email address updates.

---

### 📤 3.12 Email Server Configuration
**Description:** Administrators can register and dynamically update email server configuration settings for the system's email notifications.

- **Inputs:** Email server configuration parameters (SMTP host, port, authentication, encryption type, sender address, retry policy, etc.)
- **Outputs:** Confirmation of email server configuration update.

**Functional Requirements:**

- Provide an API endpoint to register and update SMTP server configuration details.
- Allow administrators to modify SMTP host, port, authentication credentials, encryption type, sender address, and retry policy without requiring system redeployment.
- Ensure that email settings are dynamically applied at runtime.
- The system must store email configurations securely and apply them for email notification triggers such as registration approval, password changes, and borrow requests.
- All email-related actions should be asynchronous to prevent delays in the user request processing flow.

---

### ✉️ 3.13 Email Notifications
**Description:** Automatic notifications for all major events.

**Functional Requirements:**

- Notify admins on new registration or book-related requests.
- Notify users when their registration, borrow, return, or reservation request is processed.
- Send reminders for overdue books and confirmations for profile/password updates.

---

## 🌐 4. External Interface Requirements
### 🖥️ 4.1 User Interfaces
The system will provide a responsive web interface accessible via modern browsers. It will offer distinct user experiences based on the user role (Admin or Regular User):

**i. User Dashboard:**
- Registration form with fields for name, address, occupation, phone number, and email.
- Login page with email/password input and password reset option.
- Browsing interface to search/filter books by title, author, ISBN, or category.
- Profile section to update personal information and change password.
- Borrow and return request submission panel.
- Book reservation option with visibility of current availability.
- Alerts and notifications for overdue books and request status.

**ii. Admin Dashboard:**
- Queue management interface showing pending user registrations, borrow/return requests, and reservations.
- Approve or decline actions for each request.
- Book management panel to add (single or bulk upload), edit, or remove books.
- Report generation interface for book inventory, user activity, and overdue stats.
- Notification center for new incoming requests.
- User experience will be kept minimal, clean, and intuitive to cater to all types of users.

---

### 🖧 4.2 Hardware Interfaces
The system is designed to be hardware-agnostic and will function on any standard computing device capable of running a Java backend. Key details:

**i. Server Requirements:**
- Capable of hosting a Java EE/Jakarta EE application.
- Minimum 4 GB RAM and 2 CPU cores recommended for optimal performance.
- Disk space sufficient to store logs and handle book metadata and user data.

**ii. Database Interface:**
- PostgreSQL will be hosted either locally or on a remote server.
- The application will communicate with the database via JDBC or JPA-compatible ORM (e.g., EclipseLink or Hibernate).

---

### 💻 4.3 Software Interfaces

**i. Application Server Compatibility:** 
- The backend will be a Jakarta EE-compliant application designed to run on both IBM Liberty and Payara Server, supporting deployment flexibility.

**ii. Database:**
- PostgreSQL 12 or higher will be used for persistent data storage.
- Tables will include users, books, requests, reservations, and notifications.

**iii. Email Service:**
- Integration with an external SMTP service (e.g., Gmail SMTP, SendGrid, or internal enterprise mail server).
- Used for sending:
  - Registration confirmations and approvals
  - Request status updates (borrow, return, reservation)
  - Password change confirmations
  - Overdue alerts

**iv. File Upload:**
- For bulk book uploads, the admin UI will allow uploading a CSV or Excel file.
- The backend will parse and validate this file before adding entries to the database.

---

### 📡 4.4 Communication Interfaces
All communication between the frontend and backend will happen over HTTPS using RESTful APIs. The backend will expose the following endpoints:

**i. User APIs:**
- POST `/register` – Register a new user.
- POST `/login` – Authenticate a user.
- POST `/password/change` – Change user password.
- GET `/profile` – Retrieve user profile information.
- PUT `/profile` – Update user profile.

**ii. Admin APIs:**
- GET `/requests` – Fetch the admin’s pending request queue.
- POST `/requests/approve` – Approve a pending request.
- POST `/requests/decline` – Decline a pending request.
- POST `/books/upload` – Bulk upload books from file.
- POST `/books` – Add a new book.
- PUT `/books/{id}` – Update book details.
- DELETE `/books/{id}` – Remove a book.
- GET `/reports` – Generate reports for activity, availability, etc.
- POST /email/config – Register or update the email server configuration.

**iii. Book APIs:**
- GET `/books` – Browse and filter available books.
- POST `/borrow` – Submit a borrow request.
- POST `/return` – Submit a return request.
- POST `/reserve` – Reserve a currently unavailable book.

**iv. Notification Triggers (Internal):**
- Upon request creation/approval/decline.
- Upon password or profile changes.
- Periodic job for overdue email alerts.

---

## 🏗️ 5. System Architecture
### 🌍 5.1 High-Level Architecture
The Library Management System follows a modular, layered architecture designed for scalability and ease of deployment.

**i. Frontend:**
A lightweight, browser-based client interacts with the backend via RESTful APIs. Built with standard web technologies (HTML/CSS/JavaScript) or a frontend framework such as React or Angular (optional depending on implementation scope).

**ii. Backend:**
Developed using Jakarta EE (compatible with both IBM Liberty and Payara Server). Responsible for business logic, request handling, data validation, and communication with other services like the database and email server.

**iii. Database:**
PostgreSQL is used as the relational database to store persistent data such as user accounts, book metadata, transaction history (borrow/return), and notifications.

**iv. Email Service:**
Integration with an SMTP server (e.g., SendGrid, Gmail, or enterprise mail service) enables the system to send automated notifications related to registration approvals, request statuses, overdue alerts, and password changes.

---

### 🔧 5.2 Detailed Architecture
The backend is composed of multiple loosely-coupled modules, each responsible for a specific domain within the system:

**i. User Management Module**
- Handles user registration, login, profile updates, and password changes.
- Validates user input and stores user information in the database.
- Triggers email notifications on successful registration, approval, and password changes.
- Role-based access control (e.g., Admin vs Regular User).

**ii. Book Management Module (Admin-only)**
- Allows admins to add, update, or delete book entries.
- Supports both single-book entry and bulk upload via CSV/Excel.
- Ensures data validation and consistency.
- Interfaces with the PostgreSQL database to persist book metadata.

**iii. Book Search and Browsing Module**
- Enables users to search books using filters like title, author, category, and ISBN.
- Returns a list of matching books based on the query.
- Optionally supports pagination and sorting for performance and usability.

**iv. Request Management Module**
- Manages borrow, return, and reservation requests submitted by users.
- Maintains a queue for pending requests, accessible by admins.
- Allows admins to approve or decline each request.
- Updates request status in the database and triggers email notifications accordingly.

**v. Reservation and Overdue Module**
- Tracks reservations for unavailable books.
- Ensures books are reserved on a first-come-first-serve basis.
- Sends email alerts to users when reserved books become available or are overdue.

**vi. Email Notification Module**
- The Email Notification Module is responsible for managing all outbound communication from the system via email. It serves as a bridge between the backend services and the configured SMTP server.
- This module performs the following functions:
  - Sends transactional and informational emails to users and administrators.
  - Supports the following notification types:
    - Registration confirmation
    - Registration request alert (to admins)
    - Borrow request acknowledgment, approval, and decline
    - Return and reservation confirmations
    - Overdue reminders
    - Profile and password change confirmations
    - Admin alerts for pending actions
  - All emails are sent asynchronously to prevent blocking or delays in API response handling.
  - Email content is generated using predefined templates with placeholders dynamically replaced at runtime.
  - Logs all email dispatch attempts for audit and debugging purposes.
  - Provides configuration support for SMTP parameters, which include:

| Parameter       | Description                                          |
|-----------------|------------------------------------------------------|
| SMTP Host       | Hostname or IP of the SMTP server                    |
| SMTP Port       | Port number (e.g., 587 for TLS)                      |
| Authentication  | SMTP username and password for authentication       |
| Encryption      | STARTTLS or SSL as per server capabilities           |
| From Address    | Default sender email address                        |
| Retry Policy    | Number of retries and interval between them          |

- The SMTP configuration can be dynamically registered and updated through a secured API endpoint, enabling administrators to modify email server details at runtime without the need for code changes or redeployment.

**vii. Reporting Module (Admin-only)**
- Generates reports on:
  - Book availability
  - Borrowing and returning trends
  - User activity
  - Overdue books
- Provides downloadable formats (e.g., CSV, PDF) for admin use.

---

### 🚀 5.3 Deployment and Compatibility
- **i. Application Server:** Jakarta EE application packaged as a .war file, deployable on:
  - IBM Liberty
  - Payara Server (for flexibility and vendor neutrality)

- **ii. Database Connectivity:** JDBC or JPA-based communication with PostgreSQL.
- **iii. Security:**
  - Role-based access restrictions for admin and user routes.
  - Encrypted password storage using hashing algorithms (e.g., bcrypt).
  - CSRF protection and token-based authentication for API endpoints (if needed).
**iv. Scalability Considerations:**
- Stateless REST APIs allow horizontal scaling of the backend.
- Database indexing and caching strategies (e.g., using Redis or in-memory caching) can be considered for large datasets.

---

## ⚙️ 6. Non-Functional Requirements
### 🚄 6.1. Performance
The system shall support up to 1,000 simultaneous users without performance degradation. All API responses shall be returned within 3 seconds under normal load conditions. Operations involving bulk processing, such as bulk book uploads or mass email notifications, shall be handled asynchronously to maintain responsiveness.

### 📈 6.2. Scalability
The system shall support horizontal scalability by allowing multiple instances of the application to run concurrently. The database and associated backend services shall support vertical scaling to accommodate growth in data volume and user activity.

### 🔒 6.3. Security
All user passwords shall be stored using a secure hashing algorithm (bcrypt) with salting. Communication between clients and the server shall be conducted over HTTPS to ensure encryption in transit. Role-Based Access Control (RBAC) shall be enforced to restrict access to admin-only features. Sessions shall have expiration timeouts, and users shall be able to log out explicitly. The system shall be protected against common web vulnerabilities, including SQL Injection, Cross-Site Scripting (XSS), and Cross-Site Request Forgery (CSRF).

### 🌐 6.4. Availability
The system shall be available 24 hours a day, 7 days a week, with a minimum uptime of 99.9% per calendar month. Maintenance periods shall be scheduled during off-peak hours and announced in advance. Automated health checks shall be implemented to ensure system components are operating correctly.

### 💾 6.5. Backup and Disaster Recovery
The system shall perform automated backups of all user, book, and transaction data every 24 hours. Backup files shall be securely stored and retained for a minimum of 30 days. A disaster recovery process shall be in place to restore the system from the latest backup within 2 hours of failure detection.

### 👩‍💻 6.6. Usability
The web interface shall be user-friendly and responsive, supporting modern desktop and mobile browsers. All user actions shall be accompanied by appropriate feedback messages. Admins shall have access to intuitive tools for managing users, books, and request queues.

### 🛠️ 6.7. Maintainability
The application shall follow Jakarta EE coding best practices to ensure modularity and ease of maintenance. All logs shall be centralized and formatted for structured analysis. Configuration parameters shall be externalized to allow updates without code changes or redeployment.

### 🖥️ 6.8. Portability
The backend application shall be compatible with both IBM Liberty and Payara Jakarta EE runtimes. It shall be deployable as a Docker container image.

### 📜 6.9. Compliance
The system shall comply with relevant data protection requirements in India, including ensuring user data is securely stored and only accessible to authorized personnel. Users shall be able to request account deletion, and their personal data shall be removed from the system upon request.

---

## 🔧 7. Other Requirements
### 💾 7.1 Backup and Recovery
- **Backup Frequency:** The system shall perform automated daily backups of the entire database, including user data, book records, transaction logs, and any other critical system data. Backups shall be scheduled at off-peak hours to minimize system load.
- **Backup Storage:** Backups shall be securely stored in a separate, geographically-distributed location, such as a cloud storage solution or offsite server. The backup storage should be encrypted to ensure data confidentiality.
- **Backup Retention:** Backups shall be retained for a minimum of 30 days. Older backups shall be automatically archived or deleted according to the retention policy.
- **Restore Time Objective (RTO):** The system must be capable of restoring from a backup within 2 hours of detecting a failure or system outage. The restore process shall prioritize restoring critical system components (such as the database) first, followed by non-critical data.
- **Restore Point Objective (RPO):** The system must guarantee no more than 24 hours of data loss in the event of a failure. This means that if a failure occurs, the maximum amount of data that could be lost is the data added or changed in the last 24 hours.
- **Backup Verification:** The integrity of backups must be verified periodically to ensure they can be restored successfully. Automated tests should be run to validate that backups are usable.
- **Disaster Recovery:** In the event of a catastrophic failure (e.g., hardware malfunction, natural disaster), the system shall have a disaster recovery plan in place. This plan should include procedures for recovery from backups, as well as the identification of roles and responsibilities for the recovery team.
- **Data Encryption:** Backup files shall be encrypted both in transit (when being transferred to backup storage) and at rest (when stored in backup locations) to prevent unauthorized access.

---

### 📊 7.2 Logging and Monitoring
- **System Logging:** The system shall maintain comprehensive logs of user activities, administrative actions, errors, and system events. Logs shall include timestamps, user IDs, IP addresses, and details of the action performed.
- **Log Retention:** Logs shall be retained for a minimum of 90 days. After this period, logs shall be archived or deleted according to the organization’s data retention policy.
- **Log Access:** Logs shall be accessible only to authorized personnel, and access to logs should be audited regularly to ensure compliance with security policies.
- **Real-Time Monitoring:** The system shall be monitored in real-time for any performance or security issues. Alerts shall be configured to notify administrators in the event of critical errors, high server load, or unauthorized access attempts.

---

### 🕵️‍♂️ 7.3 Audit Trail
- **Audit Logging:** All sensitive system activities, including user registration approvals, book borrowing/return requests, and administrative actions, shall be logged in an audit trail. This audit trail will help ensure accountability and traceability of all actions performed within the system.
- **Audit Retention:** The audit trail shall be retained for a minimum of 180 days and should be stored separately from regular application logs to ensure integrity.

---

### 🛡️ 7.4 User Data Privacy and Compliance
- **Data Protection:** The system shall comply with relevant data protection regulations, including GDPR (General Data Protection Regulation) and the Indian Data Protection Law, ensuring the proper handling of personal data.
- **Right to Access:** Users shall be able to access their personal data upon request, including the right to download their data in a portable format.
- **Right to Deletion:** Users shall be able to request the deletion of their account and associated data. The system shall ensure that all personal data is removed from the database once the deletion request is confirmed.
- **Data Minimization:** Only the minimum amount of personal data necessary for the operation of the system shall be collected and stored. No unnecessary data shall be retained beyond the scope of the system's requirements.

---

### 🗃️ 7.5 Data Integrity
- **Data Validation:** The system shall implement validation checks to ensure that user data (such as name, email address, and phone number) is correctly formatted and adheres to business rules.
- **Consistency Checks:** Regular data consistency checks shall be performed to ensure that all relationships between data entities (e.g., user-book interactions, borrow/return records) are consistent.
- **Error Handling:** The system shall handle data errors (such as database connection failures or invalid data input) gracefully, logging them for later review without exposing sensitive information to the user.

---

### 📚 7.6 System Documentation
- **User Documentation:** Comprehensive user documentation shall be provided, including how to register, log in, search for books, and manage book borrowing/return requests. Documentation should be clear, concise, and include screenshots or step-by-step instructions.
- **Admin Documentation:** Admins shall have access to system administration documentation detailing how to approve/reject registration requests, manage books, generate reports, and handle system recovery in the event of failure.
- **API Documentation:** A full set of API documentation shall be provided for developers, including endpoint descriptions, request/response formats, authentication details, and examples of use cases.

---

### 🌱 7.7 Environmental Requirements
- **Cross-Platform Compatibility:** The system shall be deployable on major operating systems including Linux, macOS, and Windows.
- **Browser Compatibility:** The user interface shall be compatible with the latest versions of popular web browsers, including Chrome, Firefox, Safari, and Edge.
- **Mobile Responsiveness:** The user interface shall be responsive and accessible on mobile devices, providing an optimal user experience on both smartphones and tablets.

---

### 🧑‍🏫 7.8 Training and Support
- **Training Materials:** Training materials shall be provided for both users and administrators. These materials shall cover how to use the system, troubleshoot common issues, and understand key features like book search, borrowing, and profile management.
- **Customer Support:** A customer support system shall be in place, providing users and admins with access to support via email, chat, or phone during business hours. Critical issues shall be handled with a higher priority.

---

## 📚 8. Appendices
### 📖 A1: Glossary of Terms, Concepts, and Comparisons
This section goes beyond listing abbreviations. It provides clear definitions and practical comparisons that clarify important distinctions and design decisions in the system.
**Definitions**
- The system shall define "Functional Requirements" as the specific features and behaviors that the application must implement.
- The system shall define "Non-Functional Requirements" as the constraints and quality attributes that influence the performance, scalability, and reliability of the system.
- The system shall treat "Other Requirements" as essential operational expectations such as backup policies and recovery timelines that do not directly fall under system quality attributes.
- A "Request" is any user-initiated action such as borrow, return, reserve, or register.
- A "Notification" is any system-generated message sent to the user or admin upon relevant events (e.g., approvals, reminders).
- The system shall distinguish between a "Borrow" action (for available books) and a "Reserve" action (for unavailable books).

---

**Comparison Tables**


**i. Functional vs. Non-Functional Requirements**
| Aspect              | Functional Requirements                                | Non-Functional Requirements                                  |
|---------------------|--------------------------------------------------------|-------------------------------------------------------------|
| Definition          | What the system must do                                | How the system must behave                                  |
| Scope               | User-facing features and flows                         | System qualities and technical expectations                 |
| Example             | Register user, borrow book                             | Handle 1000 concurrent users, respond within 3 seconds      |
| Measurement         | Directly testable                                      | Indirectly testable through metrics                         |

**ii. Non-Functional vs. Other Requirements**
| Aspect              | Non-Functional Requirements                            | Other Requirements                                           |
|---------------------|--------------------------------------------------------|-------------------------------------------------------------|
| Focus               | System-wide behavior and constraints                   | Operational and procedural requirements                     |
| Enforceability      | Enforced through design and code                       | Enforced via policy or infrastructure                       |
| Example             | 99.9% uptime, secure password storage                  | Daily database backups, 2-hour disaster recovery objective  |

**iii. Borrow vs. Reserve Flow**
| Action              | Description                                            | Trigger Condition          |
|---------------------|--------------------------------------------------------|-----------------------------|
| Borrow              | User borrows an available book                         | Book is currently available |
| Reserve             | User places hold on an unavailable book                | Book is currently borrowed  |

**iv. Admin vs. User Roles**
| Role                | Capabilities                                           |
|---------------------|--------------------------------------------------------|
| Admin               | Approve/decline requests, manage books, view queues, generate reports |
| User                | Register, borrow, return, reserve, update profile      |

**v. Request vs. Notification vs. API**
| Concept             | Definition                                             |
|---------------------|--------------------------------------------------------|
| Request             | A user-initiated operation (e.g., borrow, reserve)     |
| Notification        | A system-triggered email or alert                     |
| API                 | A technical interface for system interaction           |

**vi. Authentication vs. Authorization**
| Aspect             | Authentication                                  | Authorization                                   |
|--------------------|--------------------------------------------------|-------------------------------------------------|
| Definition         | Verifying the identity of a user                | Determining what an authenticated user can do  |
| Example            | Login with username and password                | Allowing only admins to approve requests       |
| Time of Evaluation | During login                                    | During each protected action                   |
| Enforced By        | Login system                                    | Role-based access control                      |

**vii. Email Notification Types**
| Notification Event               | Trigger                                               | Recipient       | Purpose                                 |
|----------------------------------|-------------------------------------------------------|-----------------|-----------------------------------------|
| Registration Confirmation        | User completes registration                           | User            | Confirm account creation               |
| Borrow Request Acknowledgment    | User submits borrow request                           | User            | Notify request submission              |
| Borrow Request Approval          | Admin approves borrow request                         | User            | Inform user of approval                |
| Borrow Request Decline           | Admin declines borrow request                         | User            | Inform user of decline                 |
| Book Return Confirmation         | User returns a book                                   | User            | Confirm successful return              |
| Overdue Reminder                 | Book return is overdue                                | User            | Remind user to return book             |
| Admin Request Queue Alert        | New borrow/reserve requests pending                   | Admin           | Notify about pending borrow/reserve requests |
| Profile Update Notification      | User updates profile/password                         | User            | Confirm profile changes                |
| Password Change Notification     | User updates password                                 | User            | Confirm password change and notify of security action |
| User Registration Request        | New user registration request submitted               | Admin           | Notify about new user registration request |

**viii. Book Request Lifecycle**
| Step              | Actor     | Description                                           |
|-------------------|-----------|--------------------------------------------------------|
| Request Submitted | User      | User initiates a borrow or reserve request             |
| Pending Approval  | System    | System adds request to admin queue                    |
| Approved/Declined | Admin     | Admin approves or declines the request                |
| Notification Sent | System    | Email sent based on approval status                   |
| Book Borrowed     | User      | User borrows the book (on approval)                   |
| Book Returned     | User      | User returns book manually or at deadline             |

**ix. API Security Mechanisms**
| Security Feature     | Description                                       | Applicable APIs            |
|----------------------|---------------------------------------------------|-----------------------------|
| Token-Based Auth     | Authenticated APIs require a token (JWT)          | All except /login, /register |
| Role Validation      | Only admins can perform certain actions           | /requests/*, /books/upload  |
| HTTPS Encryption     | All traffic must be encrypted                     | All                         |
| Input Validation     | All requests are validated against schemas        | All                         |

**x. Types of Users and Their Permissions**
| User Type  | Permissions                                                                            |
|------------|----------------------------------------------------------------------------------------|
| User       | Register, login, borrow, return, reserve books, manage their profile                   |
| Admin      | All user permissions + approve/decline requests, manage books, generate reports        |
| Guest¹     | (Planned) May be allowed to browse books before registration in future phases         |

---

### 📧 A2: Email Notification Templates and Examples

This section defines the standardized email notifications sent by the system in response to various key events. These emails are essential for keeping both users and administrators informed about important actions and statuses, ensuring clear and consistent communication. The templates are designed to fulfill specific purposes, such as confirming account creation, acknowledging requests, notifying about approval or decline of requests, and reminding users about overdue items.

Each email template is followed by a sample example to demonstrate how the message should be structured. These templates are integral to the system's notification system, ensuring that both users and administrators receive timely, accurate, and appropriate communication.

The following email types are covered in this section:
- **Registration Confirmation:** Sent to users upon successful registration.
- **Borrow Request Acknowledgment:** Sent to users when a borrow request is submitted.
- **Borrow Request Approval:** Sent to users when a borrow request is approved by an admin.
- **Borrow Request Decline:** Sent to users when a borrow request is declined by an admin.
- **Book Return Confirmation:** Sent to users upon successful return of a book.
- **Overdue Reminder:** Sent to users when a book is overdue.
- **Admin Request Queue Alert:** Sent to admins when new borrow or reserve requests are pending.
- **Profile Update Notification:** Sent to users after they update their profile or password.
- **Password Change Notification:** Sent to users after they successfully update their account password, as a confirmation and security measure.
- **User Registration Request:** Sent to admins when a new user registration request is submitted.

The purpose of these notifications is to provide clear and actionable communication to all relevant parties, ensuring smooth interactions between users and administrators in the system.

---

**i. Registration Confirmation**

Template:
```
Subject: Welcome to [Library Name]! Your Registration is Successful

Body:
Dear [User Name],

Thank you for registering with [Library Name]. Your account has been successfully created.

You can now log in using your email address and password to explore the books and other features available on our platform.

If you did not register for an account, please contact us immediately at [Support Email].

Best regards,  
[Library Name] Team
```

Example: 
```
Subject: Welcome to City Library! Your Registration is Successful

Body:
Dear John Doe,

Thank you for registering with City Library. Your account has been successfully created.

You can now log in using your email address (john.doe@example.com) and password to explore our collection of books and other services.

If you did not register for an account, please contact us immediately at support@citylibrary.com.

Best regards,  
City Library Team
```

---

**ii. Borrow Request Acknowledgment**

Template:
```
Subject: Borrow Request Received

Body:
Dear [User Name],

We have received your borrow request for the book titled "[Book Title]". Your request is being processed.

You will be notified once your borrow request has been reviewed and approved or declined by the admin.

Thank you for using [Library Name].

Best regards,  
[Library Name] Team
```

Example:
```
Subject: Borrow Request Received

Body:
Dear John Doe,

We have received your borrow request for the book titled "The Great Gatsby". Your request is being processed.

You will be notified once your borrow request has been reviewed and approved or declined by the admin.

Thank you for using City Library.

Best regards,  
City Library Team
```

---

**iii. Borrow Request Approval**

Template:
```
Subject: Your Borrow Request for [Book Title] is Approved

Body:
Dear [User Name],

We are pleased to inform you that your borrow request for the book "[Book Title]" has been approved. You can now pick up the book at your convenience.

Thank you for using [Library Name]. We hope you enjoy reading!

Best regards,  
[Library Name] Team
```

Example:
```
Subject: Your Borrow Request for "The Great Gatsby" is Approved

Body:
Dear John Doe,

We are pleased to inform you that your borrow request for the book "The Great Gatsby" has been approved. You can now pick up the book at your convenience.

Thank you for using City Library. We hope you enjoy reading!

Best regards,  
City Library Team
```

---

**iv. Borrow Request Decline**

Template:
```
Subject: Your Borrow Request for [Book Title] has been Declined

Body:
Dear [User Name],

We regret to inform you that your borrow request for the book "[Book Title]" has been declined due to [Reason for Decline].

Please feel free to browse other books available in our collection or make a new borrow request.

Best regards,  
[Library Name] Team
```

Example:
```
Subject: Your Borrow Request for "The Great Gatsby" has been Declined

Body:
Dear John Doe,

We regret to inform you that your borrow request for the book "The Great Gatsby" has been declined due to insufficient stock.

Please feel free to browse other books available in our collection or make a new borrow request.

Best regards,  
City Library Team
```

---

**v. Book Return Confirmation**

Template:
```
Subject: Book Return Successful

Body:
Dear [User Name],

We have successfully received the return of the book "[Book Title]". Thank you for returning it on time.

We hope you enjoyed reading it. Feel free to explore our other collections.

Best regards,  
[Library Name] Team
```

Example:
```
Subject: Book Return Successful

Body:
Dear John Doe,

We have successfully received the return of the book "The Great Gatsby". Thank you for returning it on time.

We hope you enjoyed reading it. Feel free to explore our other collections.

Best regards,  
City Library Team
```

---

**vi. Overdue Reminder**

Template:
```
Subject: Reminder: Your Book is Overdue

Body:
Dear [User Name],

This is a reminder that the book "[Book Title]" is overdue. Please return the book as soon as possible to avoid any late fees.

You can return the book at your convenience or contact us for any assistance.

Best regards,  
[Library Name] Team
```

Example:
```
Subject: Reminder: Your Book is Overdue

Body:
Dear John Doe,

This is a reminder that the book "The Great Gatsby" is overdue. Please return the book as soon as possible to avoid any late fees.

You can return the book at your convenience or contact us for any assistance.

Best regards,  
City Library Team
```

---

**vii. Admin Request Queue Alert**

Template:
```
Subject: New Pending Borrow/Reserve Request

Body:
Dear [Admin Name],

A new borrow or reserve request has been submitted by a user. Please review the request in the admin panel.

Request Details:
- Request Type: [Borrow/Reserve]
- User: [User Name]
- Book: [Book Title]
- Request Date: [Date]

Please approve or decline the request as needed.

Best regards,  
[Library Name] Team
```

Example:
```
Subject: New Pending Borrow/Reserve Request

Body:
Dear Admin,

A new borrow request has been submitted by a user. Please review the request in the admin panel.

Request Details:
- Request Type: Borrow
- User: John Doe
- Book: "The Great Gatsby"
- Request Date: April 15, 2025

Please approve or decline the request as needed.

Best regards,  
City Library Team
```

---

**viii. Profile Update Notification**

Template:
```
Subject: Your Profile has been Updated

Body:
Dear [User Name],

We have successfully updated your profile information. If you did not make this change, please contact us immediately at [Support Email].

Thank you for using [Library Name].

Best regards,  
[Library Name] Team
```

Example:
```
Subject: Your Profile has been Updated

Body:
Dear John Doe,

We have successfully updated your profile information. If you did not make this change, please contact us immediately at support@citylibrary.com.

Thank you for using City Library.

Best regards,  
City Library Team
```
---

**xi. Password Change Notification**

Template:
```
Subject: Your Password Has Been Updated

Body:
Dear [User Name],

We have successfully updated your account password. If you did not make this change, please contact us immediately at [Support Email].

Thank you for using [Library Name].

Best regards,
[Library Name] Team
```

Example:
```
Dear Jon Doe,

We have successfully updated your account password. If you did not make this change, please contact us immediately at support@citylibrary.org.

Thank you for using City Library.

Best regards,
City Library Team
```

---

**x. User Registration Request**

Template:
```
Subject: New User Registration Request

Body:
Dear [Admin Name],

A new user registration request has been submitted. Please review the request in the admin panel.

User Details:
- Name: [User Name]
- Email: [User Email]
- Address: [User Address]
- Occupation: [User Occupation]

Please approve or decline the registration request as needed.

Best regards,  
[Library Name] Team
```

Example:
```
Subject: New User Registration Request

Body:
Dear Admin,

A new user registration request has been submitted. Please review the request in the admin panel.

User Details:
- Name: John Doe
- Email: john.doe@example.com
- Address: 123 Main St, Cityville
- Occupation: Software Developer

Please approve or decline the registration request as needed.

Best regards,  
City Library Team
```

---

### 📝 A3: API Requests and Responses

Note: This section provides examples of key API endpoints that support the system’s functionality. While detailed API contracts should be documented separately, this summary offers essential request/response structures for clarity.

All communication between the frontend and backend will happen over HTTPS using RESTful APIs. The backend will expose the following endpoints:

**i.User APIs**

**a. POST `/register` – Register a new user.**

Request Headers:
```
Content-Type: application/json
Accept: application/json
```
Request Body:
```
{
  "email": "johndoe@example.com",
  "password": "securepassword123",
  "full_name": "John Doe"
}
```

Return Codes:
- 201 Created – Successfully registered the user.
- 400 Bad Request – Missing or invalid input fields.
- 409 Conflict – Email already in use.

Response Body (201 Created):
```
{
  "message": "User successfully registered.",
  "user_id": 12345
}
```

---

**b. POST `/login` – Authenticate a user.**

Request Headers:
```
Content-Type: application/json
Accept: application/json
```

Request Body:
```
{
  "email": "johndoe@example.com",
  "password": "securepassword123"
}
```

Return Codes:
- 200 OK – Successfully authenticated.
- 401 Unauthorized – Incorrect credentials.

Response Body (200 OK):
```
{
  "message": "Authentication successful.",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

```

---

**c. POST `/password/change` – Change user password.**

Request Headers:
```
Content-Type: application/json
Accept: application/json
Authorization: Bearer <user_token>
```

Request Body:
```
{
  "old_password": "oldpassword123",
  "new_password": "newpassword123"
}
```

Return Codes:
- 200 OK – Password successfully updated.
- 400 Bad Request – Missing or invalid input fields.
- 401 Unauthorized – Invalid old password.
- 403 Forbidden – Unauthorized attempt to change password.

Response Body (200 OK):
```
{
  "message": "Password successfully updated."
}
```

---

**d. GET `/profile` – Retrieve user profile information.**

Request Headers:
```
Authorization: Bearer <user_token>
Accept: application/json
```

Request Body:
No request body.

Return Codes:
- 200 OK – Successfully retrieved user profile.
- 401 Unauthorized – Missing or invalid authentication token.

Response Body (200 OK):
```
{
  "user_id": 12345,
  "email": "johndoe@example.com",
  "full_name": "John Doe"
}
```

---

**e. PUT `/profile` – Update user profile.**

Request Headers:
```
Content-Type: application/json
Accept: application/json
Authorization: Bearer <user_token>
```

Request Body:
```
{
  "full_name": "Johnathan Doe",
  "address": "123 Main St, Springfield, IL"
}
```

Return Codes:
- 200 OK – Profile successfully updated.
- 400 Bad Request – Missing or invalid input fields.
- 401 Unauthorized – Invalid or missing authentication token.

Response Body (200 OK):
```
{
  "message": "Profile successfully updated."
}
```

---

**ii. Admin APIs**

**a. GET `/requests` – Fetch the admin’s pending request queue.**

Request Headers:
```
Authorization: Bearer <admin_token>
Accept: application/json
```

Request Body:
No request body.

Return Codes:
- 200 OK – Successfully retrieved request queue.
- 401 Unauthorized – Missing or invalid authentication token.
- 403 Forbidden – Admin does not have permission.

Response Body (200 OK):
```
[
  {
    "request_id": 101,
    "user_id": 12345,
    "book_id": 567,
    "status": "pending"
  },
  {
    "request_id": 102,
    "user_id": 12346,
    "book_id": 568,
    "status": "pending"
  }
]
```

---

**b. POST '/requests/approve' – Approve a pending request.**

Request Headers:
```
Content-Type: application/json
Authorization: Bearer <admin_token>
Accept: application/json
```

Request Body:
```
{
  "request_id": 101
}
```

Return Codes:
- 200 OK – Successfully approved the request.
- 400 Bad Request – Invalid request ID.
- 401 Unauthorized – Invalid or missing authentication token.
- 403 Forbidden – Admin does not have permission.

Response Body (200 OK):
```
{
  "message": "Request successfully approved."
}
```

---

**c. POST `/requests/decline` – Decline a pending request.**

Request Headers:
```
Content-Type: application/json
Authorization: Bearer <admin_token>
Accept: application/json
```

Request Body:
```
{
  "request_id": 102
}
```

Return Codes:
- 200 OK – Successfully declined the request.
- 400 Bad Request – Invalid request ID.
- 401 Unauthorized – Invalid or missing authentication token.
- 403 Forbidden – Admin does not have permission.

Response Body (200 OK):
```
{
  "message": "Request successfully declined."
}
```

---

**d. POST `/books/upload` – Bulk upload books from file.**

Request Headers:
```
Content-Type: multipart/form-data
Authorization: Bearer <admin_token>
Accept: application/json
```

Request Body:
```
<Form data with file upload>
```

Return Codes:
- 200 OK – Successfully uploaded books.
- 400 Bad Request – Invalid file format or empty file.
- 401 Unauthorized – Invalid or missing authentication token.

Response Body (200 OK):
```
{
  "message": "Books successfully uploaded.",
  "uploaded_books_count": 10
}
```

---

**e. POST `/books` – Add a new book.**

Request Headers:
```
Content-Type: application/json
Authorization: Bearer <admin_token>
Accept: application/json
```

Request Body:
```
{
  "title": "The Great Book",
  "author": "John Author",
  "isbn": "978-1234567890",
  "quantity": 10
}
```

Return Codes:
- 201 Created – Successfully added the book.
- 400 Bad Request – Invalid book details.
- 401 Unauthorized – Invalid or missing authentication token.

Response Body (201 Created):
```
{
  "message": "Book successfully added.",
  "book_id": 12345
}
```

---

**f. PUT `/books/{id}` – Update book details.**

Request Headers:
```
Content-Type: application/json
Authorization: Bearer <admin_token>
Accept: application/json
```

Request Body:
```
{
  "title": "The Great Book Revised",
  "author": "John Author",
  "isbn": "978-1234567890",
  "quantity": 15
}
```

Return Codes:
- 200 OK – Successfully updated the book.
- 400 Bad Request – Invalid book details.
- 401 Unauthorized – Invalid or missing authentication token.
- 404 Not Found – Book not found.

Response Body (200 OK):
```
{
  "message": "Book successfully updated."
}
```

---

**g. DELETE `/books/{id}` – Remove a book.**

Request Headers:
```
Authorization: Bearer <admin_token>
Accept: application/json
```

Request Body:
No request body.

Return Codes:
- 200 OK – Successfully removed the book.
- 401 Unauthorized – Invalid or missing authentication token.
- 404 Not Found – Book not found.

Response Body (200 OK):
```
{
  "message": "Book successfully removed."
}
```

---

**h. GET `/reports` – Generate reports for activity, availability, etc.**

Request Headers:
```
Authorization: Bearer <admin_token>
Accept: application/json
```

Request Body:
```
{
  "report_type": "user_activity",
  "date_range": "2022-01-01 to 2022-12-31"
}
```

Return Codes:
- 200 OK – Successfully generated the report.
- 400 Bad Request – Invalid report parameters.
- 401 Unauthorized – Invalid or missing authentication token.

Response Body (200 OK):
```
{
  "message": "Report successfully generated.",
  "report_data": {
    "total_requests": 500,
    "total_borrows": 450
  }
}
```

---

**i. POST `/email/config` – Register or update the email server configuration.**

Request Headers:
```
Content-Type: application/json
Authorization: Bearer <admin_token>
Accept: application/json
```

Request Body:
```
{
  "smtp_host": "smtp.example.com",
  "smtp_port": 587,
  "smtp_user": "email_user",
  "smtp_pass": "email_password",
  "encryption": "TLS",
  "from_address": "no-reply@example.com"
}
```

Return Codes:
- 200 OK – Successfully updated email configuration.
- 400 Bad Request – Invalid configuration details.
- 401 Unauthorized – Invalid or missing authentication token.

Response Body (200 OK):
```
{
  "message": "Email server configuration updated."
}
```

---

**iii. Book APIs**

**a. GET `/books` – Browse and filter available books.**

Request Headers:
```
Accept: application/json
```

Request Body:
No request body.

Return Codes:
- 200 OK – Successfully retrieved books.
- 400 Bad Request – Invalid filters.

Response Body (200 OK):
```
[
  {
    "book_id": 12345,
    "title": "The Great Book",
    "author": "John Author",
    "quantity_available": 10
  },
  {
    "book_id": 12346,
    "title": "Another Book",
    "author": "Jane Author",
    "quantity_available": 5
  }
]
```

---

**b. POST `/borrow` – Submit a borrow request.**

Request Headers:
```
Content-Type: application/json
Authorization: Bearer <user_token>
Accept: application/json
```

Request Body:
```
{
  "book_id": 12345,
  "borrow_duration": "7"
}
```

Return Codes:
- 200 OK – Successfully borrowed the book.
- 400 Bad Request – Invalid request data.
- 401 Unauthorized – Invalid or missing authentication token.
- 404 Not Found – Book not available.

Response Body (200 OK):
```
{
  "message": "Book successfully borrowed.",
  "due_date": "2022-12-01"
}
```

---

**c. POST `/return` – Submit a return request.**

Request Headers:
```
Content-Type: application/json
Authorization: Bearer <user_token>
Accept: application/json
```

Request Body:
```
{
  "book_id": 12345
}
```

Return Codes:
- 200 OK – Successfully returned the book.
- 400 Bad Request – Invalid request data.
- 401 Unauthorized – Invalid or missing authentication token.
- 404 Not Found – Book not found.

Response Body (200 OK):
```
{
  "message": "Book successfully returned."
}
```

---

**d. POST `/reserve` – Reserve a currently unavailable book.**

Request Headers:
```
Content-Type: application/json
Authorization: Bearer <user_token>
Accept: application/json
```

Request Body:
```
{
  "book_id": 12345
}
```

Return Codes:

- 200 OK – Successfully reserved the book.
- 400 Bad Request – Invalid request data.
- 401 Unauthorized – Invalid or missing authentication token.
- 404 Not Found – Book not available.

Response Body (200 OK):
```
{
  "message": "Book successfully reserved."
}
```
