# Library Management System: Flow Charts

## 1. User Registration Flow

1. Start: Process begins.
2. User Opens Registration Form: User navigates to the registration page.
3. User Enters Details: User fills out required fields (Name, Email, Password).
4. System Validates Input: System checks the validity of user input (e.g., format, required fields).
5. Validation Check: If the input is invalid, errors are shown and the user is asked to re-enter details.
6. User Creation in DB: If the input is valid, the system creates the user in the database.
7. Send Confirmation Email: A confirmation email is sent to the user.
8. User Successfully Registered: The registration is completed successfully.
9. End: Process ends

<img width="499" alt="Screenshot 2025-04-15 at 5 52 34 PM" src="https://github.com/user-attachments/assets/0cf3a683-d19a-4f91-a36b-34d17e9c6745" />

---

## 2. Borrow Request Flow

1. User logs into the system
2. User searches for a book
3. User selects a book to borrow
4. System checks if book is available
5. If available, system creates borrow request
6. Notify admin for approval

<img width="649" alt="Screenshot 2025-04-15 at 5 53 52 PM" src="https://github.com/user-attachments/assets/40cac16e-54aa-4555-8a5f-3f19f68654e3" />

---

## 3. Borrow Approval / Decline Flow

1. Admin receives borrow request notification
2. Admin reviews the request
3. Admin approves or declines the request
4. If approved, update borrow status and notify user
5. If declined, notify user

<img width="696" alt="Screenshot 2025-04-15 at 5 55 12 PM" src="https://github.com/user-attachments/assets/12d726e5-05b5-4a3c-bedb-2b288fa9e5e1" />

---

## 4. Return Book Flow

1. User initiates return
2. System verifies borrow record
3. System updates book status to available
4. System logs return action
5. Notify user of successful return

<img width="454" alt="Screenshot 2025-04-15 at 5 56 41 PM" src="https://github.com/user-attachments/assets/b8888e1b-dc41-4738-9413-f97cee524d44" />

---

## 5. Overdue Reminder Flow

1. Scheduled job runs to check due dates
2. System finds overdue records
3. System sends reminder notifications

<img width="690" alt="Screenshot 2025-04-15 at 5 57 47 PM" src="https://github.com/user-attachments/assets/8580aaf2-846a-4206-b3b5-3834f79ae313" />

---

## 6. Profile / Password Update Flow

1. User accesses profile settings
2. User updates info or password
3. System validates input
4. System saves changes
5. Notify user of update

<img width="405" alt="Screenshot 2025-04-15 at 5 59 41 PM" src="https://github.com/user-attachments/assets/4aed4ef1-281b-4fab-9a5c-efbd8c5f1668" />

---

## 7. Bulk Upload Books via CSV

1. Admin uploads CSV
2. System parses CSV
3. System validates each entry
4. System saves valid entries to DB
5. Shows result summary (success/failure)

<img width="469" alt="Screenshot 2025-04-15 at 6 00 58 PM" src="https://github.com/user-attachments/assets/6aad5148-fc61-4ecb-9b40-c50e22c9eb6c" />

---

## 8. Add or Update Book Entry

1. Admin opens book form
2. Admin fills or updates book details
3. System validates form data
4. System updates or inserts record into DB
5. Notify admin

<img width="407" alt="Screenshot 2025-04-15 at 6 02 08 PM" src="https://github.com/user-attachments/assets/1754f258-ffad-45e3-93bc-93461d383533" />

---

## 9. Generate Reports

1. Admin selects report type
2. System fetches data
3. System generates report
4. Report is shown or downloaded

<img width="619" alt="Screenshot 2025-04-15 at 6 03 08 PM" src="https://github.com/user-attachments/assets/9432079f-e791-4abd-b504-7d584c6fdc16" />

---

## 10. Configure SMTP Server

1. Admin enters SMTP configuration
2. System validates config
3. System saves settings
4. Sends test email to confirm

<img width="572" alt="Screenshot 2025-04-15 at 6 04 21 PM" src="https://github.com/user-attachments/assets/bb710eff-c490-4d6c-b0f1-9625f1c7e22e" />

---

## 11. Book Reservation Flow

1. User searches for book
2. Book is not available
3. User reserves the book
4. System logs reservation
5. Notify user when book becomes available

<img width="397" alt="Screenshot 2025-04-15 at 6 05 32 PM" src="https://github.com/user-attachments/assets/d96c1728-cdff-41f7-b731-afdb5678cd49" />

---

## 12. Search Books Flow

1. User enters search query
2. System queries DB
3. Results are returned and displayed

<img width="430" alt="Screenshot 2025-04-15 at 6 06 48 PM" src="https://github.com/user-attachments/assets/76cdfb63-ffc8-440b-8ba1-843276e6a21f" />

---

## 13. Book Availability Check Flow

1. System receives book ID
2. System queries DB
3. Returns availability status

<img width="504" alt="Screenshot 2025-04-15 at 6 07 50 PM" src="https://github.com/user-attachments/assets/b3fd3df5-0af4-4a9e-82ac-b169f49c2348" />

---

## 14. User Login Flow

1. User enters credentials
2. System authenticates user
3. If valid, generate session/token
4. Redirect to dashboard

<img width="394" alt="Screenshot 2025-04-15 at 6 11 46 PM" src="https://github.com/user-attachments/assets/baef53ea-ce09-435f-9d8c-e9e2788a49e7" />

---

## 15. Admin Role Assignment Flow

1. Admin selects user
2. Admin assigns role
3. System updates role in DB
4. Notify user

<img width="301" alt="Screenshot 2025-04-15 at 6 12 51 PM" src="https://github.com/user-attachments/assets/6686723e-4232-46c3-bfed-9c8604c4358f" />

---

## 16. User Notification Flow

1. Event occurs (borrow/return/overdue)
2. System triggers notification
3. Notification is sent via email/portal

<img width="477" alt="Screenshot 2025-04-15 at 6 14 10 PM" src="https://github.com/user-attachments/assets/2b98c506-1626-4a67-b5c9-bda6651b3f8d" />

---

## 17. Payment / Fees Flow

1. User has overdue book or fine
2. System calculates fee
3. User proceeds to payment
4. System processes payment
5. Update user account

<img width="315" alt="Screenshot 2025-04-15 at 6 15 37 PM" src="https://github.com/user-attachments/assets/512bcdf6-2d8a-475e-b177-2a7543584167" />



