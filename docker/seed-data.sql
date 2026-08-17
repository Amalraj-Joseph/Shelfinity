-- Shelfinity sample/test data.
--
-- This must be run AFTER the backend has started at least once, because the
-- `users`, `books`, `queue_items` and `reservations` tables are created by
-- JPA schema generation (persistence.xml), not by init-db.sql. Postgres's
-- docker-entrypoint-initdb.d scripts run before the backend ever starts, so
-- this file is intentionally NOT wired into that mechanism.
--
-- Run it manually once the stack (postgres + backend) is up:
--   docker exec -i shelfinity-postgres psql -U shelfinity -d shelfinity < docker/seed-data.sql
-- or, for a native `mvn liberty:dev` backend against a local postgres:
--   psql "postgresql://shelfinity:shelfinity@localhost:5432/shelfinity" -f docker/seed-data.sql
--
-- Safe to re-run: every row uses a fixed id with ON CONFLICT DO NOTHING.
--
-- The keycloak_id values below match the "id" fields pinned in
-- docker/keycloak/realm-shelfinity.json, so these users can actually log in
-- (username/password are the same as the realm file, e.g. john.doe/john123).

-- ---------------------------------------------------------------------------
-- Users (mirrors the accounts provisioned in realm-shelfinity.json)
-- ---------------------------------------------------------------------------
INSERT INTO users (id, keycloak_id, email, name, role, is_active, created_at, updated_at) VALUES
    ('10000000-0000-4000-8000-000000000001', '8a56642a-e116-5268-8ef4-d5ec7ca792ba', 'admin@shelfinity.com', 'Shelfinity Admin', 'ADMIN', true, now() - interval '90 days', NULL),
    ('10000000-0000-4000-8000-000000000002', 'cefa3b5e-c6a8-58f7-bb67-1aa410dc27e5', 'john.doe@shelfinity.com', 'John Doe', 'USER', true, now() - interval '60 days', NULL),
    ('10000000-0000-4000-8000-000000000003', 'ffd4b310-a94d-574c-a637-a82f42cb3015', 'jane.smith@shelfinity.com', 'Jane Smith', 'USER', true, now() - interval '45 days', NULL),
    ('10000000-0000-4000-8000-000000000004', 'ee280384-e1d1-50b9-bec9-fc2e9e796d17', 'bob.wilson@shelfinity.com', 'Bob Wilson', 'USER', true, now() - interval '40 days', NULL),
    ('10000000-0000-4000-8000-000000000005', 'e744ff73-2890-5896-9d7e-174a0d611fa9', 'alice.johnson@shelfinity.com', 'Alice Johnson', 'USER', true, now() - interval '30 days', NULL),
    ('10000000-0000-4000-8000-000000000006', '96af4046-332e-54ae-9eb6-ed949cedbb3f', 'mike.brown@shelfinity.com', 'Mike Brown', 'USER', true, now() - interval '20 days', NULL),
    ('10000000-0000-4000-8000-000000000007', '73dcdb89-e4ff-55ec-b81d-d3c64fda42be', 'sarah.davis@shelfinity.com', 'Sarah Davis', 'USER', true, now() - interval '10 days', NULL),
    ('10000000-0000-4000-8000-000000000008', '740c02db-6f3a-5ace-96f0-e2efc3124179', 'david.miller@shelfinity.com', 'David Miller', 'USER', false, now() - interval '5 days', now() - interval '1 days')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Books: a spread of fully available, partially checked out, fully checked
-- out, and admin-disabled copies, plus nullable-field edge cases.
-- ---------------------------------------------------------------------------
INSERT INTO books (id, title, author, isbn, description, available, totalcopies, available_copies, created_at, updated_at) VALUES
    ('20000000-0000-4000-8000-000000000001', 'The Great Gatsby', 'F. Scott Fitzgerald', '978-0-7432-7356-5', 'A classic American novel set in the Jazz Age.', true, 5, 5, now() - interval '80 days', NULL),
    ('20000000-0000-4000-8000-000000000002', '1984', 'George Orwell', '978-0-452-28423-4', 'A dystopian social science fiction novel.', true, 3, 0, now() - interval '80 days', now() - interval '2 days'),
    ('20000000-0000-4000-8000-000000000003', 'To Kill a Mockingbird', 'Harper Lee', '978-0-06-112008-4', 'A novel about racial injustice in the American South.', true, 4, 2, now() - interval '75 days', now() - interval '3 days'),
    ('20000000-0000-4000-8000-000000000004', 'Pride and Prejudice', 'Jane Austen', '978-0-14-143951-8', 'A romantic novel of manners.', true, 6, 6, now() - interval '75 days', NULL),
    ('20000000-0000-4000-8000-000000000005', 'The Catcher in the Rye', 'J.D. Salinger', '978-0-316-76948-0', 'A story about teenage rebellion and alienation.', true, 3, 1, now() - interval '70 days', now() - interval '12 days'),
    ('20000000-0000-4000-8000-000000000006', 'Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', '978-0-439-70818-8', 'The first book in the Harry Potter series.', true, 8, 8, now() - interval '70 days', NULL),
    ('20000000-0000-4000-8000-000000000007', 'The Hobbit', 'J.R.R. Tolkien', '978-0-547-92822-7', 'A fantasy novel and children''s book.', true, 5, 0, now() - interval '65 days', now() - interval '20 days'),
    ('20000000-0000-4000-8000-000000000008', 'Brave New World', 'Aldous Huxley', '978-0-06-085052-4', 'A dystopian novel set in a futuristic World State.', true, 4, 4, now() - interval '65 days', NULL),
    ('20000000-0000-4000-8000-000000000009', 'The Lord of the Rings', 'J.R.R. Tolkien', '978-0-618-64561-5', 'An epic high-fantasy novel.', true, 7, 3, now() - interval '60 days', now() - interval '4 days'),
    ('20000000-0000-4000-8000-000000000010', 'Animal Farm', 'George Orwell', '978-0-452-28424-1', 'An allegorical novella about Soviet totalitarianism.', true, 5, 5, now() - interval '60 days', NULL),
    ('20000000-0000-4000-8000-000000000011', 'The Chronicles of Narnia', 'C.S. Lewis', '978-0-06-076489-7', 'A series of seven fantasy novels.', true, 6, 6, now() - interval '55 days', NULL),
    ('20000000-0000-4000-8000-000000000012', 'Moby-Dick', 'Herman Melville', NULL, NULL, true, 3, 3, now() - interval '50 days', NULL),
    ('20000000-0000-4000-8000-000000000013', 'War and Peace', 'Leo Tolstoy', '978-0-14-303999-0', 'A historical novel set during the Napoleonic Wars. Currently withdrawn from circulation.', false, 1, 0, now() - interval '50 days', now() - interval '15 days'),
    ('20000000-0000-4000-8000-000000000014', 'The Odyssey', 'Homer', '978-0-14-026886-7', 'An ancient Greek epic poem.', true, 5, 5, now() - interval '45 days', NULL),
    ('20000000-0000-4000-8000-000000000015', 'Dune', 'Frank Herbert', '978-0-441-01359-3', 'A science fiction saga of politics, religion and ecology on a desert planet.', true, 4, 1, now() - interval '30 days', now() - interval '6 days'),
    ('20000000-0000-4000-8000-000000000016', 'Foundation', 'Isaac Asimov', '978-0-553-29335-0', 'The first novel in Asimov''s Foundation series.', true, 2, 0, now() - interval '25 days', now() - interval '18 days')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Queue items: pending/approved/rejected borrow requests, a pending return,
-- and two overdue approved borrows (for testing /overdue and /reports).
-- ---------------------------------------------------------------------------
INSERT INTO queue_items (id, type, user_keycloak_id, book_id, status, description, adminremark, created_at, updated_at, processed_at, processed_by, due_date) VALUES
    -- John requests Dune; still pending admin approval.
    ('30000000-0000-4000-8000-000000000001', 'BOOK_BORROW', 'cefa3b5e-c6a8-58f7-bb67-1aa410dc27e5', '20000000-0000-4000-8000-000000000015', 'PENDING', 'Would like to borrow Dune for a book club.', NULL, now() - interval '1 days', NULL, NULL, NULL, NULL),
    -- Jane borrowed The Great Gatsby; approved, due in the future.
    ('30000000-0000-4000-8000-000000000002', 'BOOK_BORROW', 'ffd4b310-a94d-574c-a637-a82f42cb3015', '20000000-0000-4000-8000-000000000001', 'APPROVED', 'Borrow request', 'Approved, enjoy the read.', now() - interval '3 days', now() - interval '2 days', now() - interval '2 days', '8a56642a-e116-5268-8ef4-d5ec7ca792ba', now() + interval '11 days'),
    -- Bob borrowed The Hobbit; approved but overdue.
    ('30000000-0000-4000-8000-000000000003', 'BOOK_BORROW', 'ee280384-e1d1-50b9-bec9-fc2e9e796d17', '20000000-0000-4000-8000-000000000007', 'APPROVED', 'Borrow request', 'Approved.', now() - interval '20 days', now() - interval '19 days', now() - interval '19 days', '8a56642a-e116-5268-8ef4-d5ec7ca792ba', now() - interval '5 days'),
    -- Alice borrowed Foundation; approved and further overdue.
    ('30000000-0000-4000-8000-000000000004', 'BOOK_BORROW', 'e744ff73-2890-5896-9d7e-174a0d611fa9', '20000000-0000-4000-8000-000000000016', 'APPROVED', 'Borrow request', 'Approved.', now() - interval '25 days', now() - interval '24 days', now() - interval '24 days', '8a56642a-e116-5268-8ef4-d5ec7ca792ba', now() - interval '10 days'),
    -- Mike's request for War and Peace was rejected (book withdrawn).
    ('30000000-0000-4000-8000-000000000005', 'BOOK_BORROW', '96af4046-332e-54ae-9eb6-ed949cedbb3f', '20000000-0000-4000-8000-000000000013', 'REJECTED', 'Borrow request', 'Book is currently withdrawn from circulation.', now() - interval '15 days', now() - interval '14 days', now() - interval '14 days', '8a56642a-e116-5268-8ef4-d5ec7ca792ba', NULL),
    -- Sarah wants to return To Kill a Mockingbird; pending admin processing.
    ('30000000-0000-4000-8000-000000000006', 'BOOK_RETURN', '73dcdb89-e4ff-55ec-b81d-d3c64fda42be', '20000000-0000-4000-8000-000000000003', 'PENDING', 'Returning book, finished reading.', NULL, now() - interval '1 days', NULL, NULL, NULL, NULL),
    -- David's earlier return of 1984 was approved (historical record).
    ('30000000-0000-4000-8000-000000000007', 'BOOK_RETURN', '740c02db-6f3a-5ace-96f0-e2efc3124179', '20000000-0000-4000-8000-000000000002', 'APPROVED', 'Returning book.', 'Return processed, thank you.', now() - interval '7 days', now() - interval '6 days', now() - interval '6 days', '8a56642a-e116-5268-8ef4-d5ec7ca792ba', NULL)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Reservations: one of every lifecycle status, tied to the fully-checked-out
-- books above (1984, The Hobbit, Foundation).
-- ---------------------------------------------------------------------------
INSERT INTO reservations (id, user_keycloak_id, book_id, status, created_at, updated_at, notified_at, expires_at, notes) VALUES
    -- Mike is waiting on 1984 (no copies available right now).
    ('40000000-0000-4000-8000-000000000001', '96af4046-332e-54ae-9eb6-ed949cedbb3f', '20000000-0000-4000-8000-000000000002', 'ACTIVE', now() - interval '2 days', NULL, NULL, now() + interval '5 days', NULL),
    -- Sarah was notified that The Hobbit is available and has 2 days left to claim it.
    ('40000000-0000-4000-8000-000000000002', '73dcdb89-e4ff-55ec-b81d-d3c64fda42be', '20000000-0000-4000-8000-000000000007', 'NOTIFIED', now() - interval '5 days', now() - interval '1 days', now() - interval '1 days', now() + interval '2 days', NULL),
    -- John previously reserved Foundation and went on to borrow it.
    ('40000000-0000-4000-8000-000000000003', 'cefa3b5e-c6a8-58f7-bb67-1aa410dc27e5', '20000000-0000-4000-8000-000000000016', 'FULFILLED', now() - interval '30 days', now() - interval '25 days', now() - interval '26 days', now() - interval '19 days', NULL),
    -- Jane cancelled her reservation on Dune.
    ('40000000-0000-4000-8000-000000000004', 'ffd4b310-a94d-574c-a637-a82f42cb3015', '20000000-0000-4000-8000-000000000015', 'CANCELLED', now() - interval '10 days', now() - interval '8 days', NULL, now() - interval '3 days', 'Cancelled by user.'),
    -- Bob's reservation on The Hobbit expired before he could claim it.
    ('40000000-0000-4000-8000-000000000005', 'ee280384-e1d1-50b9-bec9-fc2e9e796d17', '20000000-0000-4000-8000-000000000007', 'EXPIRED', now() - interval '25 days', now() - interval '18 days', now() - interval '20 days', now() - interval '18 days', NULL)
ON CONFLICT (id) DO NOTHING;
