# As-Is Scenario

The current Library Management System is fully manual and relies on traditional paper-based processes. All operations — ranging from book cataloging to issuing and returning books — are recorded in physical ledgers maintained by the library staff. The system has evolved little over time and lacks any form of automation or digitization.

This section outlines how the existing manual system operates, highlighting key areas of functionality and the pain points that result from the absence of a modern solution.

---

### 1. Book Inventory Management

- All books are logged into physical ledgers upon arrival.
- Details such as title, author, publisher, year of publication, and genre are handwritten by staff.
- To check if a book is available, staff must browse the shelves or search the log manually.
- If a book is missing or misplaced, there is no quick way to trace it.
  
💡 Problem: Time-consuming search process, possibility of misfiling or duplication, lack of audit trail for additions or removals

---

### 2. Member Registration and Management

- New members fill out paper forms which are filed manually.
- Membership details (e.g., name, address, phone, occupation) are stored in folders or register books.
- Any updates to member data require physical editing or writing in a new entry.

💡 Problem: Inefficient data retrieval and no real-time updates. High chance of inconsistencies and lost data.

---

### 3. Book Lending and Return

- When a member borrows a book, their name, book title, issue date, and due date are written in a lending ledger.
- Returns are similarly tracked manually by finding the original entry and marking it as returned.
- If a book is returned late, the fine is calculated by the librarian on the spot.

💡 Problem: Lack of reminders or tracking system for overdue books, potential for data mismatches, and no automated fine calculations.

---

### 4. Reservation and Waiting List

- If a book is not available, members can ask to be added to a physical waiting list maintained on a notepad or register.
- When the book becomes available, staff must manually check the list and contact the next person—usually by phone or during their next visit.

💡 Problem: No transparency for members to know their position in the queue. Manual follow-ups delay access to books.

---

### 5. Communication and Notifications

- All communication is face-to-face or by phone. There is no email or SMS system for reminders or notifications.
- Members are expected to remember due dates or check them manually by calling the library or visiting in person.

💡 Problem: No proactive notifications, leading to forgotten due dates and late returns.

---

### 6. Reports and Analytics

- Reports on popular books, overdue items, and member activity must be created manually by reviewing ledgers.
- This process can take hours or even days, depending on the time period being evaluated.

💡 Problem: Limited insight for decision-making. Staff spends excessive time creating reports instead of serving members.

---

### 7. Administrative Operations

- Every process—be it issuing new cards, approving member registrations, or auditing borrow/return records—is manual.
- Library staff often face backlogs, especially during peak seasons like semester beginnings or holiday periods.

💡 Problem: High operational overhead. Prone to human error, bottlenecks, and inefficiencies.

---

### Summary Table

| Area                | Manual Process Summary |
|---------------------|------------------------|
| Book Inventory      | Handwritten entries in ledgers, physical search required. |
| Member Management   | Paper forms, file cabinets, no easy updates or search. |
| Lending and Return  | Ledger-based tracking, manual due date and fine calculations. |
| Reservations        | Physical waiting list, no automatic queuing or notification. |
| Notifications       | No automated reminders; relies on memory or manual follow-up. |
| Reporting           | Time-consuming manual report creation by scanning ledgers. |
| Admin Workload      | High. All tasks are physical and repetitive. |

---

### Limitations of the Current System

📉 Inefficient and time-consuming processes.

⚠️ High likelihood of data inconsistency or loss.

📂 Difficult to scale as the number of books or members grows.

🔒 No access control or role-based permission.

📊 No ability to generate quick, accurate reports.

🧾 Lack of accountability and traceability of transactions.

📞 Poor communication and notification capability.

---

This scenario serves as the baseline for the proposed digital system. The goal is to address these pain points by automating core operations, improving accessibility, and reducing administrative overhead through a modern Jakarta EE-based application.
