/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState, useEffect } from 'react';
import {
  Grid,
  Column,
  DataTable,
  TableContainer,
  Table,
  TableHead,
  TableRow,
  TableHeader,
  TableBody,
  TableCell,
  TableToolbar,
  TableToolbarContent,
  TableToolbarSearch,
  Button,
  Dropdown,
  Pagination,
  Tag,
  Loading
} from '@carbon/react';
import './BookList.scss';

const LoadingSpinner = () => (
  <div className="loading-container">
    <Loading description="Loading books..." withOverlay={false} />
  </div>
);

const ErrorMessage = ({ message, onRetry }) => (
  <div className="error-container">
    <p>{message}</p>
    <Button onClick={onRetry} kind="tertiary">Try Again</Button>
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
  const [pageSize, setPageSize] = useState(10);

  const API_BASE_URL = '/api';

  const filterOptions = [
    { id: 'all', text: 'All Books' },
    { id: 'available', text: 'Available' },
    { id: 'unavailable', text: 'Unavailable' }
  ];

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
    setCurrentPage(1);
  }, [books, searchTerm, filter]);

  const handleRequestBook = async (bookId) => {
    if (requesting.has(bookId)) return;

    try {
      setRequesting(prev => new Set(prev).add(bookId));
      
      const token = localStorage.getItem('authToken');
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

  const headers = [
    { key: 'title', header: 'Title' },
    { key: 'author', header: 'Author' },
    { key: 'isbn', header: 'ISBN' },
    { key: 'category', header: 'Category' },
    { key: 'copies', header: 'Copies' },
    { key: 'status', header: 'Status' },
    { key: 'actions', header: 'Actions' }
  ];

  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = startIndex + pageSize;
  const paginatedBooks = filteredBooks.slice(startIndex, endIndex);

  const rows = paginatedBooks.map(book => ({
    id: book.id.toString(),
    title: book.title,
    author: book.author,
    isbn: book.isbn,
    category: book.category || 'N/A',
    copies: `${book.availableCopies || 0}/${book.copies || 1}`,
    status: book.available ? 'available' : 'unavailable',
    available: book.available,
    bookId: book.id
  }));

  return (
    <div className="book-list">
      <Grid>
        <Column sm={4} md={8} lg={16}>
          <div className="book-list-header">
            <h1>Book Collection</h1>
            <p>Browse and request books from our library</p>
          </div>
        </Column>
      </Grid>

      <Grid>
        <Column sm={4} md={8} lg={16}>
          <DataTable rows={rows} headers={headers}>
            {({
              rows,
              headers,
              getHeaderProps,
              getRowProps,
              getTableProps,
              getTableContainerProps
            }) => (
              <TableContainer
                title={`Showing ${filteredBooks.length} of ${books.length} books`}
                {...getTableContainerProps()}
              >
                <TableToolbar>
                  <TableToolbarContent>
                    <TableToolbarSearch
                      placeholder="Search books by title, author, or ISBN..."
                      onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <Dropdown
                      id="filter-dropdown"
                      titleText=""
                      label="Filter by status"
                      items={filterOptions}
                      itemToString={(item) => (item ? item.text : '')}
                      selectedItem={filterOptions.find(opt => opt.id === filter)}
                      onChange={({ selectedItem }) => setFilter(selectedItem.id)}
                    />
                  </TableToolbarContent>
                </TableToolbar>
                <Table {...getTableProps()}>
                  <TableHead>
                    <TableRow>
                      {headers.map((header) => (
                        <TableHeader key={header.key} {...getHeaderProps({ header })}>
                          {header.header}
                        </TableHeader>
                      ))}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {rows.map((row) => (
                      <TableRow key={row.id} {...getRowProps({ row })}>
                        {row.cells.map((cell) => {
                          if (cell.info.header === 'status') {
                            return (
                              <TableCell key={cell.id}>
                                <Tag
                                  type={cell.value === 'available' ? 'green' : 'red'}
                                  size="sm"
                                >
                                  {cell.value === 'available' ? 'Available' : 'Unavailable'}
                                </Tag>
                              </TableCell>
                            );
                          }
                          if (cell.info.header === 'actions') {
                            const bookData = paginatedBooks.find(b => b.id.toString() === row.id);
                            return (
                              <TableCell key={cell.id}>
                                {bookData?.available ? (
                                  <Button
                                    size="sm"
                                    onClick={() => handleRequestBook(bookData.id)}
                                    disabled={requesting.has(bookData.id)}
                                  >
                                    {requesting.has(bookData.id) ? 'Requesting...' : 'Request'}
                                  </Button>
                                ) : (
                                  <Button size="sm" kind="ghost" disabled>
                                    Not Available
                                  </Button>
                                )}
                              </TableCell>
                            );
                          }
                          return <TableCell key={cell.id}>{cell.value}</TableCell>;
                        })}
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
                <Pagination
                  page={currentPage}
                  pageSize={pageSize}
                  pageSizes={[10, 20, 30, 40, 50]}
                  totalItems={filteredBooks.length}
                  onChange={({ page, pageSize }) => {
                    setCurrentPage(page);
                    setPageSize(pageSize);
                  }}
                />
              </TableContainer>
            )}
          </DataTable>
        </Column>
      </Grid>
    </div>
  );
};

export default BookList;

// Made with Bob
