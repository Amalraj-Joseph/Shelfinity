/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState, useEffect } from 'react';
import './BookList.css';

const LoadingSpinner = () => (
  <div className="loading-spinner">
    <div className="spinner"></div>
    <p>Loading books...</p>
  </div>
);

const ErrorMessage = ({ message, onRetry }) => (
  <div className="error-message">
    <p>{message}</p>
    <button onClick={onRetry} className="btn-retry">Try Again</button>
  </div>
);

const BookList = ({ authToken }) => {
  const [books, setBooks] = useState([]);
  const [filteredBooks, setFilteredBooks] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [filter, setFilter] = useState('all');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [requesting, setRequesting] = useState(new Set());
  const [currentPage, setCurrentPage] = useState(1);
  const [booksPerPage] = useState(12);

  const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:9080/shelfinity-backend/app';

  const fetchBooks = async () => {
    try {
      setLoading(true);
      setError(null);

      if (!authToken) {
        throw new Error('Authentication required');
      }

      const response = await fetch(`${API_BASE_URL}/books`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error('Failed to fetch books');
      }

      const booksData = await response.json();
      setBooks(booksData);
      setFilteredBooks(booksData);
    } catch (err) {
      setError(err.message);
      console.error('Error fetching books:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (authToken) {
      fetchBooks();
    }
  }, [authToken]);

  useEffect(() => {
    const filtered = books.filter(book => {
      const matchesSearch = book.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                           book.author.toLowerCase().includes(searchTerm.toLowerCase()) ||
                           book.isbn.toLowerCase().includes(searchTerm.toLowerCase());
      
      const matchesFilter = filter === 'all' || 
                           (filter === 'available' && book.available) ||
                           (filter === 'unavailable' && !book.available);
      
      return matchesSearch && matchesFilter;
    });
    
    setFilteredBooks(filtered);
    setCurrentPage(1); // Reset to first page when filtering
  }, [books, searchTerm, filter]);

  const handleRequestBook = async (bookId) => {
    if (requesting.has(bookId)) return;

    try {
      setRequesting(prev => new Set(prev).add(bookId));
      
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('Please log in to request books');
      }

      const response = await fetch(`${API_BASE_URL}/queues`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          bookId: bookId,
          type: 'BORROW',
          status: 'PENDING'
        })
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to request book');
      }

      // Update the book availability
      setBooks(prevBooks => 
        prevBooks.map(book => 
          book.id === bookId 
            ? { ...book, available: false, availableCopies: Math.max(0, book.availableCopies - 1) }
            : book
        )
      );

      alert('Book requested successfully!');
    } catch (err) {
      alert(err.message);
      console.error('Error requesting book:', err);
    } finally {
      setRequesting(prev => {
        const newSet = new Set(prev);
        newSet.delete(bookId);
        return newSet;
      });
    }
  };

  const handleRetry = () => {
    fetchBooks();
  };

  // Pagination
  const indexOfLastBook = currentPage * booksPerPage;
  const indexOfFirstBook = indexOfLastBook - booksPerPage;
  const currentBooks = filteredBooks.slice(indexOfFirstBook, indexOfLastBook);
  const totalPages = Math.ceil(filteredBooks.length / booksPerPage);

  const paginate = (pageNumber) => setCurrentPage(pageNumber);

  if (loading) {
    return (
      <div className="book-list">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="book-list">
        <ErrorMessage message={error} onRetry={handleRetry} />
      </div>
    );
  }

  return (
    <div className="book-list">
      <div className="book-list-header">
        <h1>Book Collection</h1>
        <p>Browse and request books from our library</p>
        <div className="book-count">
          Showing {filteredBooks.length} of {books.length} books
        </div>
      </div>

      <div className="book-controls">
        <div className="search-container">
          <input
            type="text"
            placeholder="Search books by title, author, or ISBN..."
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
        {currentBooks.map(book => (
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
                Copies: {book.availableCopies || 0}/{book.copies || 1} available
              </p>
            </div>

            <div className="book-actions">
              {book.available ? (
                <button
                  onClick={() => handleRequestBook(book.id)}
                  className="btn-request"
                  disabled={requesting.has(book.id)}
                >
                  {requesting.has(book.id) ? 'Requesting...' : 'Request Book'}
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

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <button 
            onClick={() => paginate(currentPage - 1)} 
            disabled={currentPage === 1}
            className="pagination-btn"
          >
            Previous
          </button>
          
          <div className="page-numbers">
            {Array.from({ length: totalPages }, (_, i) => i + 1).map(number => (
              <button
                key={number}
                onClick={() => paginate(number)}
                className={`pagination-btn ${currentPage === number ? 'active' : ''}`}
              >
                {number}
              </button>
            ))}
          </div>
          
          <button 
            onClick={() => paginate(currentPage + 1)} 
            disabled={currentPage === totalPages}
            className="pagination-btn"
          >
            Next
          </button>
        </div>
      )}

      {filteredBooks.length === 0 && (
        <div className="no-books">
          <p>No books found matching your criteria.</p>
          <button onClick={() => { setSearchTerm(''); setFilter('all'); }} className="btn-clear-filters">
            Clear Filters
          </button>
        </div>
      )}
    </div>
  );
};

export default BookList;
