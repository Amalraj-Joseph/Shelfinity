-- Seed data for Shelfinity Library Management System
-- This script populates the database with sample data for testing

\c shelfinity

-- Insert sample books
INSERT INTO book (id, title, author, isbn, description, total_copies, available_copies, created_at, updated_at) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'The Great Gatsby', 'F. Scott Fitzgerald', '978-0-7432-7356-5', 'A classic American novel set in the Jazz Age', 5, 5, NOW(), NOW()),
('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '1984', 'George Orwell', '978-0-452-28423-4', 'A dystopian social science fiction novel', 3, 3, NOW(), NOW()),
('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'To Kill a Mockingbird', 'Harper Lee', '978-0-06-112008-4', 'A novel about racial injustice in the American South', 4, 4, NOW(), NOW()),
('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'Pride and Prejudice', 'Jane Austen', '978-0-14-143951-8', 'A romantic novel of manners', 6, 6, NOW(), NOW()),
('e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'The Catcher in the Rye', 'J.D. Salinger', '978-0-316-76948-0', 'A story about teenage rebellion and alienation', 3, 3, NOW(), NOW()),
('f5eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', '978-0-439-70818-8', 'The first book in the Harry Potter series', 8, 8, NOW(), NOW()),
('g6eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'The Hobbit', 'J.R.R. Tolkien', '978-0-547-92822-7', 'A fantasy novel and children''s book', 5, 5, NOW(), NOW()),
('h7eebc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'Brave New World', 'Aldous Huxley', '978-0-06-085052-4', 'A dystopian novel set in a futuristic World State', 4, 4, NOW(), NOW()),
('i8eebc99-9c0b-4ef8-bb6d-6bb9bd380a99', 'The Lord of the Rings', 'J.R.R. Tolkien', '978-0-618-64561-5', 'An epic high-fantasy novel', 7, 7, NOW(), NOW()),
('j9eebc99-9c0b-4ef8-bb6d-6bb9bd380aaa', 'Animal Farm', 'George Orwell', '978-0-452-28424-1', 'An allegorical novella about Soviet totalitarianism', 5, 5, NOW(), NOW()),
('k0eebc99-9c0b-4ef8-bb6d-6bb9bd380abb', 'The Chronicles of Narnia', 'C.S. Lewis', '978-0-06-076489-7', 'A series of seven fantasy novels', 6, 6, NOW(), NOW()),
('l1eebc99-9c0b-4ef8-bb6d-6bb9bd380acc', 'Moby-Dick', 'Herman Melville', '978-0-14-243724-7', 'The narrative of Captain Ahab''s obsessive quest', 3, 3, NOW(), NOW()),
('m2eebc99-9c0b-4ef8-bb6d-6bb9bd380add', 'War and Peace', 'Leo Tolstoy', '978-0-14-303999-0', 'A historical novel set during the Napoleonic Wars', 4, 4, NOW(), NOW()),
('n3eebc99-9c0b-4ef8-bb6d-6bb9bd380aee', 'The Odyssey', 'Homer', '978-0-14-026886-7', 'An ancient Greek epic poem', 5, 5, NOW(), NOW()),
('o4eebc99-9c0b-4ef8-bb6d-6bb9bd380aff', 'Jane Eyre', 'Charlotte Brontë', '978-0-14-144114-6', 'A novel about the experiences of the title character', 4, 4, NOW(), NOW());

-- Note: Users will be created through Keycloak and then synced to the database
-- The application will handle user creation when they first log in

-- Insert sample email configuration (optional - for testing email notifications)
INSERT INTO email_config (id, smtp_host, smtp_port, sender_email, sender_name, username, password, use_tls, use_ssl, require_auth, active, created_at) VALUES
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'smtp.gmail.com', 587, 'noreply@shelfinity.com', 'Shelfinity Library', 'noreply@shelfinity.com', 'your-app-password', true, false, true, false, NOW());

-- Note: The email configuration above is inactive by default (active = false)
-- Administrators should update this through the API with real SMTP credentials

COMMIT;

-- Made with Bob
