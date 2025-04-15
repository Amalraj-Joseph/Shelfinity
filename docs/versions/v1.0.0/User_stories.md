# Library Management System: User Stories and Use Cases

## 1. Introduction
This document outlines the User Stories and Use Cases for the Library Management System. These elements help define the system's behavior from both high-level (User Stories) and detailed (Use Cases) perspectives. User stories capture the user’s needs in a simple, concise format, while use cases provide a more thorough description of the system's interactions.

## 2. User Stories
### What are User Stories?
User stories are brief, informal descriptions of a feature or function from the perspective of the user. They help identify the user’s need and desired outcome. Typically, user stories follow this format:

- **As a [type of user]**, I want to **[do something]** so that I can **[achieve a goal]**.

User stories are typically used in agile development processes to quickly define requirements and ensure the development team focuses on delivering valuable features to users.

---

### User Stories for Library Management System

**a). User Registration**

- **As a** new user, **I want to** sign up with my personal details **so that I can** access the library system and borrow books.

---

**b). Login**

- **As a** registered user, **I want to** log in with my username and password **so that I can** access my account and borrow/return books.

---

**c). Search for Books**

- **As a** user, **I want to** search for books by title, author, or genre **so that I can** find books I want to borrow.

---

**d). Borrow Books**

- **As a** registered user, **I want to** borrow available books **so that I can** read them.

---

**e). Return Books**

- **As a** user, **I want to** return borrowed books **so that I can** make space for new books and comply with library policies.

---

**f). Book Availability**

- **As a** user, **I want to** know whether a book is available for borrowing **so that I can** decide if I want to borrow it or place a hold.

---

**g). Reserve Books**

- **As a** user, **I want to** place a hold on unavailable books **so that I can** borrow them when they become available.

---

**h). Change Password**

- **As a** user, **I want to** change my password **so that I can** keep my account secure.

---

**i). Update Personal Information**

- **As a** user, **I want to** update my personal information, such as name, email address, or phone number **so that I can** keep my contact details up to date in the system.

---

**j). Admin - Set Up Email Server**

- **As an** admin, I want to configure the email server settings (SMTP server, email templates, etc.) **so that I can** send email notifications to users regarding their requests and account activities.

---

**k). Admin - Approve User Registration**

- **As an** admin, I want to approve or reject user registration requests **so that I can** manage the library’s user base.

---

**l). Admin - Manage Borrow Requests**

- **As an** admin, I want to approve or decline borrow requests **so that I can** ensure users only borrow books available in the library.

---

**m). Admin - Manage Return Requests**

- **As an** admin, **I want to** approve or decline book return requests **so that I can** keep the system up to date with available inventory.

---

**n). Admin - Send Notifications**

- **As an** admin, **I want to** send email notifications to users when their requests are approved/declined **so that they are** informed of the status of their requests.

---

**o). Book Inventory Management**

- **As an** admin, **I want to** add or remove books from the inventory **so that I can** keep the library up to date with available resources.

---

**p). Admin - View User Queue**

- **As an** admin, **I want to** view the list of pending user requests (such as registration, borrow, or return) **so that I can manage** them in an organized way.

---

**q). Email Notification for Status Change**

- **As a** user, **I want to** receive an email when my request (user registration, borrow, or return request) status is changed **so that I can** stay informed.

---

## 3. Use Cases
### What are Use Cases?

A use case is a more detailed description of how a system should behave when interacting with a user or another system. It describes the sequence of steps involved in achieving a goal, covering normal, alternative, and exceptional flows.

Each use case includes:

- **Title:** A descriptive name.
- **Primary Actor:** Who is involved in the interaction (e.g., user, admin).
- **Goal:** What the primary actor wants to achieve.
- **Main Flow:** The series of steps for achieving the goal.
- **Alternate Flow:** Variations for handling exceptions.
- **Postconditions:** The system’s state after the use case is completed.

---

### Use Cases for Library Management System
**a.) Register a User**

- **Primary Actor:** New User
- **Goal:** To sign up and create an account.
- **Preconditions:** The user is not already registered.
- **Main Flow:**
  1. The user fills out the registration form with name, address, occupation, phone number, and email.
  2. The system submits the registration request to the admin queue.
  3. The admin reviews and approves/declines the registration.
  4. If approved, the user receives an email and can log in.
- **Postconditions:** User is added to the system or registration is declined.

---

**b.) Login to the System**

- **Primary Actor:** Registered User
- **Goal:** To log into the system.
- **Preconditions:** The user has an active account.
- **Main Flow:**
  1. The user enters their username and password.
  2. The system validates the credentials.
  3. If valid, the user is logged in and redirected to the main page.
- **Postconditions:** The user is logged into the system.

---

**c.) Search for Books**

- **Primary Actor:** User
- **Goal:** To search for books by title, author, or genre.
- **Main Flow:**
  1. The user enters a search term in the search bar.
  2. The system displays relevant search results.
- **Postconditions:** User sees a list of books matching the search term.

---

**d.) Borrow a Book**

- **Primary Actor:** Registered User
- **Goal:** To borrow a book from the library.
- **Main Flow:**
  1. The user selects a book to borrow.
  2. The system checks if the book is available.
  3. If available, the book is checked out to the user.
  4. The user receives confirmation.
- **Postconditions:** The book is checked out to the user.

---

**e.) Return a Book**

- **Primary Actor:** User
- **Goal:** To return a borrowed book.
- **Main Flow:**
  1. The user selects the book they want to return.
  2. The system checks if the book was borrowed.
  3. The book is marked as returned, and the user receives confirmation.
- **Postconditions:** The book is returned and available for others to borrow.

---

**f.) Admin - Approve/Decline User Registration**

- **Primary Actor:** Admin
- **Goal:** To approve or decline user registration requests.
- **Main Flow:**
  1. The admin views the registration queue.
  2. The admin selects a request and approves or declines it.
  3. The user receives an email with the status of their registration.
- **Postconditions:** The user account is either created or the registration is declined.

---

**g.) Admin - Manage Borrow/Return Requests**

- **Primary Actor:** Admin
- **Goal:** To approve or decline borrow or return requests.
- **Main Flow:**
  1. The admin views the borrow/return queue.
  2. The admin approves or declines the request.
  3. The user receives an email about the status of their request.
- **Postconditions:** The book is either checked out or returned, or the request is declined.

---

**h.) Send Email Notification**

- **Primary Actor:** Admin/System
- **Goal:** To send email notifications to users when their request status changes.
- Main Flow:
  1. The admin or system updates a request's status.
  2. The system sends an email to the user informing them of the status change.
- **Postconditions:** The user is notified via email.

---

**i.) Set Up Email Server**

- **Primary Actor:** Admin
- **Goal:** To configure the email server settings (SMTP server, email templates).
- **Preconditions:** Admin has access to the system’s admin panel.
- **Main Flow:**
  1. The admin accesses the system's email configuration settings.
  2. The admin enters SMTP server details (host, port, username, password).
  3. The admin configures email templates for different notifications (registration, borrow, return).
  4. The admin saves the configuration.
- **Postconditions:** The system is ready to send emails using the configured settings.

---

**j.) Update Personal Information**

- **Primary Actor:** Registered User
- **Goal:** To update personal information (name, email address, phone number).
- **Preconditions:** The user is logged in to their account.
- **Main Flow:**
  1. The user navigates to the "Profile Settings" section.
  2. The user updates their personal information (name, email, phone).
  3. The user submits the changes.
  4. The system updates the user’s information in the database.
  5. The user receives a confirmation that their profile has been updated successfully.
- **Postconditions:** The user’s personal information is updated in the system.

---

**k.) Change Password**

- **Primary Actor:** Registered User
- **Goal:** To change the account password.
- **Preconditions:** The user is logged in and knows their current password.
- **Main Flow:**
  1. The user navigates to the "Change Password" section in their profile settings.
  2. The user enters their current password.
  3. The user enters a new password and confirms it.
  4. The system validates the password strength and confirms the new password.
  5. The user receives a confirmation that their password has been updated successfully.
- **Postconditions:** The user’s password is updated in the system.

---

## Appendix: Comparison of User Stories and Use Cases

| Aspect | User Story | Use Case |
|--------|------------|----------|
| Focus  | Focuses on the user's needs and goals in a simple format.| Describes detailed interaction between a user and the system. |
| Detail Level | High-level, brief, and informal.| More detailed, often structured with steps and flows. |
| Scope | Focuses on a small feature or function.| Describes an entire process or system interaction. |
| Format | Typically follows the format: "As a [user], I want [feature] so that [benefit]." | Describes multiple steps and flows (main and alternate). |
| Purpose | Used to define the general user need and expected outcome. | Used to detail how a system should behave in different scenarios. |
| Usage | Primarily used in Agile for quick, flexible development. | Used for comprehensive system design and requirements gathering. |
| Audience | Mainly for development teams to implement features. | Typically used for developers, business analysts, and system architects. |

---

