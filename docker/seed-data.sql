-- Seed data for Shelfinity development environment
-- This script populates the database with initial data for testing and development

-- Connect to the shelfinity database
\c shelfinity;

-- Create initial users
INSERT INTO users (id, keycloak_id, email, name, role, is_active, created_at, updated_at) VALUES
    (gen_random_uuid(), '20e0e2e1-9f52-4fa9-bd6a-9d2fcd1c7a01', 'admin@shelfinity.com', 'Shelfinity Admin', 'ADMIN', true, NOW(), NOW()),
    (gen_random_uuid(), '6e7e9b5a-1b2c-4aa8-9e86-3dbe49f0f101', 'john.doe@shelfinity.com', 'John Doe', 'USER', true, NOW(), NOW()),
    (gen_random_uuid(), '2a9c1f88-fb86-44a9-8f6c-0ed9e1a3f202', 'jane.smith@shelfinity.com', 'Jane Smith', 'USER', true, NOW(), NOW()),
    (gen_random_uuid(), '77c5b8f0-a1c2-4fb3-95d3-1f6a0cf7e303', 'bob.wilson@shelfinity.com', 'Bob Wilson', 'USER', true, NOW(), NOW()),
    (gen_random_uuid(), '8d0a1b2c-3e4f-5a6b-7c8d-9e0f1a2b3c44', 'alice.johnson@shelfinity.com', 'Alice Johnson', 'USER', true, NOW(), NOW()),
    (gen_random_uuid(), 'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c55', 'mike.brown@shelfinity.com', 'Mike Brown', 'USER', true, NOW(), NOW()),
    (gen_random_uuid(), 'b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d66', 'sarah.davis@shelfinity.com', 'Sarah Davis', 'USER', true, NOW(), NOW()),
    (gen_random_uuid(), 'c3d4e5f6-a7b8-4c9d-0e1f-2a3b4c5d6e77', 'david.miller@shelfinity.com', 'David Miller', 'USER', true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Create initial books
INSERT INTO books (id, isbn, title, author, description, totalcopies, available_copies, available, created_at, updated_at) VALUES
    (gen_random_uuid(), '978-0-7475-3269-9', 'Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', 'The first novel in the Harry Potter series, following the young wizard Harry Potter as he begins his education at Hogwarts School of Witchcraft and Wizardry.', 5, 3, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-7475-3849-3', 'Harry Potter and the Chamber of Secrets', 'J.K. Rowling', 'The second novel in the Harry Potter series, where Harry returns to Hogwarts for his second year and discovers a mysterious chamber.', 4, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-7475-4624-5', 'Harry Potter and the Prisoner of Azkaban', 'J.K. Rowling', 'The third novel in the Harry Potter series, where Harry learns about his past and the truth about his parents'' death.', 4, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-7475-5100-3', 'Harry Potter and the Goblet of Fire', 'J.K. Rowling', 'The fourth novel in the Harry Potter series, where Harry is unexpectedly entered into a dangerous tournament.', 3, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-7475-5101-0', 'Harry Potter and the Order of the Phoenix', 'J.K. Rowling', 'The fifth novel in the Harry Potter series, where Harry faces the growing threat of Voldemort and the Ministry''s denial.', 3, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-7475-8108-6', 'Harry Potter and the Half-Blood Prince', 'J.K. Rowling', 'The sixth novel in the Harry Potter series, where Harry learns about Voldemort''s past and prepares for the final battle.', 3, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-7475-8109-3', 'Harry Potter and the Deathly Hallows', 'J.K. Rowling', 'The final novel in the Harry Potter series, where Harry and his friends complete their quest to defeat Voldemort.', 3, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-06-112008-4', 'To Kill a Mockingbird', 'Harper Lee', 'A classic novel about racial injustice in the American South, told through the eyes of young Scout Finch.', 4, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-14-028333-4', '1984', 'George Orwell', 'A dystopian novel about totalitarianism and surveillance society, following the life of Winston Smith.', 3, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-679-72327-6', 'The Great Gatsby', 'F. Scott Fitzgerald', 'A novel about the American Dream and the Jazz Age, following the mysterious millionaire Jay Gatsby.', 3, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-7432-7356-5', 'The Catcher in the Rye', 'J.D. Salinger', 'A novel about teenage alienation and loss of innocence, following the adventures of Holden Caulfield.', 3, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-679-41063-4', 'Pride and Prejudice', 'Jane Austen', 'A romantic novel about the relationship between Elizabeth Bennet and Mr. Darcy in Georgian-era England.', 4, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-14-143951-8', 'Jane Eyre', 'Charlotte Brontë', 'A novel about the orphaned Jane Eyre and her journey to find love and independence.', 3, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-14-143947-1', 'Wuthering Heights', 'Emily Brontë', 'A novel about the passionate and destructive love between Catherine Earnshaw and Heathcliff.', 3, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-679-42023-4', 'The Lord of the Rings: The Fellowship of the Ring', 'J.R.R. Tolkien', 'The first volume of The Lord of the Rings, following the journey of Frodo Baggins and the Fellowship.', 4, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-345-33971-4', 'The Lord of the Rings: The Two Towers', 'J.R.R. Tolkien', 'The second volume of The Lord of the Rings, where the Fellowship is broken and the quest continues.', 4, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-345-33973-8', 'The Lord of the Rings: The Return of the King', 'J.R.R. Tolkien', 'The final volume of The Lord of the Rings, where the quest reaches its climax and conclusion.', 4, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-06-231500-7', 'The Alchemist', 'Paulo Coelho', 'A novel about following your dreams and listening to your heart, following the journey of Santiago.', 3, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-06-112241-5', 'The Kite Runner', 'Khaled Hosseini', 'A novel about redemption and friendship, set against the backdrop of Afghanistan''s turbulent history.', 3, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-316-06536-6', 'The Book Thief', 'Markus Zusak', 'A novel about a young girl''s relationship with her foster parents and the books she steals during Nazi Germany.', 3, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-316-06856-5', 'The Fault in Our Stars', 'John Green', 'A novel about two teenagers who meet at a cancer support group and fall in love.', 4, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-316-06856-6', 'Looking for Alaska', 'John Green', 'A novel about a teenager who attends boarding school and experiences love, loss, and self-discovery.', 3, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-316-06856-7', 'Paper Towns', 'John Green', 'A novel about a teenager who goes on a road trip to find his missing neighbor and discovers himself along the way.', 3, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-316-06856-8', 'Turtles All the Way Down', 'John Green', 'A novel about a teenager with obsessive-compulsive disorder who investigates the disappearance of a billionaire.', 3, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-316-06856-9', 'The Anthropocene Reviewed', 'John Green', 'A collection of essays reviewing various aspects of human-centered planet Earth.', 2, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-316-06857-0', 'The Martian', 'Andy Weir', 'A novel about an astronaut who is stranded on Mars and must find a way to survive and return to Earth.', 3, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-316-06857-1', 'Project Hail Mary', 'Andy Weir', 'A novel about an astronaut who wakes up alone on a spaceship with no memory of how he got there.', 3, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-316-06857-2', 'Artemis', 'Andy Weir', 'A novel about a smuggler living in the first city on the Moon who gets caught up in a conspiracy.', 3, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-316-06857-3', 'Ready Player One', 'Ernest Cline', 'A novel about a teenager who participates in a virtual reality treasure hunt in a dystopian future.', 3, 1, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-316-06857-4', 'Ready Player Two', 'Ernest Cline', 'A sequel to Ready Player One, following the protagonist''s new adventure in virtual reality.', 3, 2, true, NOW(), NOW()),
    (gen_random_uuid(), '978-0-316-06857-5', 'Armada', 'Ernest Cline', 'A novel about a teenager who discovers that a video game he plays is actually a training simulation for an alien invasion.', 3, 1, true, NOW(), NOW())
ON CONFLICT (isbn) DO NOTHING;

-- Create initial queue items (book requests)
INSERT INTO queue_items (id, user_keycloak_id, book_id, type, status, description, created_at, updated_at) VALUES
    (gen_random_uuid(), 'john-uuid', (SELECT id FROM books WHERE title = 'The Great Gatsby' LIMIT 1), 'BORROW', 'PENDING', 'Request to borrow The Great Gatsby', NOW(), NOW()),
    (gen_random_uuid(), 'jane-uuid', (SELECT id FROM books WHERE title = 'Pride and Prejudice' LIMIT 1), 'BORROW', 'APPROVED', 'Request to borrow Pride and Prejudice', NOW(), NOW()),
    (gen_random_uuid(), 'bob-uuid', (SELECT id FROM books WHERE title = '1984' LIMIT 1), 'BORROW', 'PENDING', 'Request to borrow 1984', NOW(), NOW()),
    (gen_random_uuid(), 'alice-uuid', (SELECT id FROM books WHERE title = 'The Catcher in the Rye' LIMIT 1), 'BORROW', 'REJECTED', 'Request to borrow The Catcher in the Rye', NOW(), NOW()),
    (gen_random_uuid(), 'mike-uuid', (SELECT id FROM books WHERE title = 'The Alchemist' LIMIT 1), 'BORROW', 'APPROVED', 'Request to borrow The Alchemist', NOW(), NOW()),
    (gen_random_uuid(), 'sarah-uuid', (SELECT id FROM books WHERE title = 'The Kite Runner' LIMIT 1), 'BORROW', 'PENDING', 'Request to borrow The Kite Runner', NOW(), NOW()),
    (gen_random_uuid(), 'david-uuid', (SELECT id FROM books WHERE title = 'The Book Thief' LIMIT 1), 'BORROW', 'APPROVED', 'Request to borrow The Book Thief', NOW(), NOW()),
    (gen_random_uuid(), 'john-uuid', (SELECT id FROM books WHERE title = 'The Martian' LIMIT 1), 'BORROW', 'PENDING', 'Request to borrow The Martian', NOW(), NOW()),
    (gen_random_uuid(), 'jane-uuid', (SELECT id FROM books WHERE title = 'Ready Player One' LIMIT 1), 'BORROW', 'APPROVED', 'Request to borrow Ready Player One', NOW(), NOW()),
    (gen_random_uuid(), 'bob-uuid', (SELECT id FROM books WHERE title = 'The Fault in Our Stars' LIMIT 1), 'BORROW', 'PENDING', 'Request to borrow The Fault in Our Stars', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Display summary of seeded data
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Books' as table_name, COUNT(*) as count FROM books
UNION ALL
SELECT 'Queue Items' as table_name, COUNT(*) as count FROM queue_items;
