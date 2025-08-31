/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState } from 'react';
import './BookList.css';

const BookList = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [filter, setFilter] = useState('all');

  const mockBooks = [
    {
      id: 1,
      title: 'The Great Gatsby',
      author: 'F. Scott Fitzgerald',
      isbn: '978-0743273565',
      available: true,
      copies: 3,
      availableCopies: 2,
      category: 'Fiction'
    },
    {
      id: 2,
      title: '1984',
      author: 'George Orwell',
      isbn: '978-0451524935',
      available: true,
      copies: 2,
      availableCopies: 1,
      category: 'Fiction'
    },
    {
      id: 3,
      title: 'To Kill a Mockingbird',
      author: 'Harper Lee',
      isbn: '978-0446310789',
      available: false,
      copies: 1,
      availableCopies: 0,
      category: 'Fiction'
    },
    {
      id: 4,
      title: 'Pride and Prejudice',
      author: 'Jane Austen',
      isbn: '978-0141439518',
      available: true,
      copies: 4,
      availableCopies: 3,
      category: 'Fiction'
    },
    {
      id: 5,
      title: 'The Hobbit',
      author: 'J.R.R. Tolkien',
      isbn: '978-0547928241',
      available: true,
      copies: 2,
      availableCopies: 1,
      category: 'Fantasy'
    },
    {
      id: 6,
      title: 'Dune',
      author: 'Frank Herbert',
      isbn: '978-0441172719',
      available: true,
      copies: 3,
      availableCopies: 2,
      category: 'Science Fiction'
    }
  ];

  const filteredBooks = mockBooks.filter(book => {
    const matchesSearch = book.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         book.author.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesFilter = filter === 'all' || 
                         (filter === 'available' && book.available) ||
                         (filter === 'unavailable' && !book.available);
    return matchesSearch && matchesFilter;
  });

  const handleRequestBook = (bookId) => {
    // TODO: Implement book request functionality
    console.log(`Requesting book ${bookId}`);
  };

  return (
    <div className="book-list">
      <div className="book-list-header">
        <h1>Book Collection</h1>
        <p>Browse and request books from our library</p>
      </div>

      <div className="book-controls">
        <div className="search-container">
          <input
            type="text"
            placeholder="Search books by title or author..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>

        <div className="filter-container">
          <select
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            className="filter-select"
          >
            <option value="all">All Books</option>
            <option value="available">Available</option>
            <option value="unavailable">Unavailable</option>
          </select>
        </div>
      </div>

      <div className="books-grid">
        {filteredBooks.map(book => (
          <div key={book.id} className="book-card">
            <div className="book-header">
              <h3 className="book-title">{book.title}</h3>
              <span className={`book-status ${book.available ? 'available' : 'unavailable'}`}>
                {book.available ? 'Available' : 'Unavailable'}
              </span>
            </div>
            
            <div className="book-details">
              <p className="book-author">by {book.author}</p>
              <p className="book-isbn">ISBN: {book.isbn}</p>
              <p className="book-category">Category: {book.category}</p>
              <p className="book-copies">
                Copies: {book.availableCopies}/{book.copies} available
              </p>
            </div>

            <div className="book-actions">
              {book.available ? (
                <button
                  onClick={() => handleRequestBook(book.id)}
                  className="btn-request"
                >
                  Request Book
                </button>
              ) : (
                <button className="btn-unavailable" disabled>
                  Not Available
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {filteredBooks.length === 0 && (
        <div className="no-books">
          <p>No books found matching your criteria.</p>
        </div>
      )}
    </div>
  );
};

export default BookList;
