/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reports;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.shelfinity.books.Book;
import com.shelfinity.books.BookRepository;
import com.shelfinity.queues.QueueItem;
import com.shelfinity.queues.QueueRepository;
import com.shelfinity.queues.QueueStatus;
import com.shelfinity.queues.QueueType;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

/**
 * Service for generating various reports and analytics.
 */
@ApplicationScoped
@Transactional
public class ReportService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    private BookRepository bookRepository;
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private QueueRepository queueRepository;
    
    /**
     * Get book popularity report (most borrowed books).
     */
    public List<BookPopularityReport> getBookPopularityReport(int limit) {
        String jpql = "SELECT q.bookId, COUNT(q) as borrowCount " +
                     "FROM QueueItem q " +
                     "WHERE q.type = :type AND q.status = :status " +
                     "GROUP BY q.bookId " +
                     "ORDER BY borrowCount DESC";
        
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("type", QueueType.BOOK_BORROW);
        query.setParameter("status", QueueStatus.APPROVED);
        query.setMaxResults(limit);
        
        List<Object[]> results = query.getResultList();
        List<BookPopularityReport> reports = new ArrayList<>();
        
        for (Object[] result : results) {
            java.util.UUID bookId = (java.util.UUID) result[0];
            Long borrowCount = (Long) result[1];
            
            bookRepository.findById(bookId).ifPresent(book -> {
                reports.add(new BookPopularityReport(
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    borrowCount.intValue()
                ));
            });
        }
        
        return reports;
    }
    
    /**
     * Get borrowing trends over time.
     */
    public BorrowingTrendsReport getBorrowingTrends(LocalDateTime startDate, LocalDateTime endDate) {
        // Get total borrows in period
        String borrowQuery = "SELECT COUNT(q) FROM QueueItem q " +
                           "WHERE q.type = :type AND q.status = :status " +
                           "AND q.createdAt BETWEEN :start AND :end";
        
        TypedQuery<Long> query = entityManager.createQuery(borrowQuery, Long.class);
        query.setParameter("type", QueueType.BOOK_BORROW);
        query.setParameter("status", QueueStatus.APPROVED);
        query.setParameter("start", startDate);
        query.setParameter("end", endDate);
        
        Long totalBorrows = query.getSingleResult();
        
        // Get total returns in period
        String returnQuery = "SELECT COUNT(q) FROM QueueItem q " +
                           "WHERE q.type = :type AND q.status = :status " +
                           "AND q.createdAt BETWEEN :start AND :end";
        
        TypedQuery<Long> returnQueryTyped = entityManager.createQuery(returnQuery, Long.class);
        returnQueryTyped.setParameter("type", QueueType.BOOK_RETURN);
        returnQueryTyped.setParameter("status", QueueStatus.APPROVED);
        returnQueryTyped.setParameter("start", startDate);
        returnQueryTyped.setParameter("end", endDate);
        
        Long totalReturns = returnQueryTyped.getSingleResult();
        
        // Get currently borrowed books
        String activeQuery = "SELECT COUNT(q) FROM QueueItem q " +
                           "WHERE q.type = :type AND q.status = :status";
        
        TypedQuery<Long> activeQueryTyped = entityManager.createQuery(activeQuery, Long.class);
        activeQueryTyped.setParameter("type", QueueType.BOOK_BORROW);
        activeQueryTyped.setParameter("status", QueueStatus.APPROVED);
        
        Long currentlyBorrowed = activeQueryTyped.getSingleResult();
        
        return new BorrowingTrendsReport(
            startDate,
            endDate,
            totalBorrows.intValue(),
            totalReturns.intValue(),
            currentlyBorrowed.intValue()
        );
    }
    
    /**
     * Get user activity report.
     */
    public List<UserActivityReport> getUserActivityReport(int limit) {
        String jpql = "SELECT q.userKeycloakId, COUNT(q) as activityCount " +
                     "FROM QueueItem q " +
                     "WHERE q.status = :status " +
                     "GROUP BY q.userKeycloakId " +
                     "ORDER BY activityCount DESC";
        
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("status", QueueStatus.APPROVED);
        query.setMaxResults(limit);
        
        List<Object[]> results = query.getResultList();
        List<UserActivityReport> reports = new ArrayList<>();
        
        for (Object[] result : results) {
            String userKeycloakId = (String) result[0];
            Long activityCount = (Long) result[1];
            
            userRepository.findByKeycloakId(userKeycloakId).ifPresent(user -> {
                reports.add(new UserActivityReport(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    activityCount.intValue()
                ));
            });
        }
        
        return reports;
    }
    
    /**
     * Get library statistics summary.
     */
    public LibraryStatistics getLibraryStatistics() {
        // Total books
        long totalBooks = bookRepository.findAll().size();
        
        // Available books
        long availableBooks = bookRepository.findAvailable().size();
        
        // Total users
        long totalUsers = userRepository.findAll().size();
        
        // Active borrows
        String activeBorrowsQuery = "SELECT COUNT(q) FROM QueueItem q " +
                                   "WHERE q.type = :type AND q.status = :status";
        TypedQuery<Long> activeBorrowsQueryTyped = entityManager.createQuery(activeBorrowsQuery, Long.class);
        activeBorrowsQueryTyped.setParameter("type", QueueType.BOOK_BORROW);
        activeBorrowsQueryTyped.setParameter("status", QueueStatus.APPROVED);
        long activeBorrows = activeBorrowsQueryTyped.getSingleResult();
        
        // Pending requests
        long pendingRequests = queueRepository.countPending();
        
        // Overdue items
        long overdueItems = queueRepository.findOverdueItems().size();
        
        return new LibraryStatistics(
            (int) totalBooks,
            (int) availableBooks,
            (int) totalUsers,
            (int) activeBorrows,
            (int) pendingRequests,
            (int) overdueItems
        );
    }
    
    /**
     * Get author distribution report.
     */
    public List<AuthorDistribution> getAuthorDistribution() {
        List<Book> allBooks = bookRepository.findAll();
        
        Map<String, Integer> authorCount = new HashMap<>();
        for (Book book : allBooks) {
            String author = book.getAuthor() != null ? book.getAuthor() : "Unknown";
            authorCount.put(author, authorCount.getOrDefault(author, 0) + 1);
        }
        
        return authorCount.entrySet().stream()
            .map(entry -> new AuthorDistribution(entry.getKey(), entry.getValue()))
            .sorted((a, b) -> b.getCount() - a.getCount())
            .collect(Collectors.toList());
    }
    
    // Report DTOs
    
    public static class BookPopularityReport {
        private java.util.UUID bookId;
        private String title;
        private String author;
        private int borrowCount;
        
        public BookPopularityReport(java.util.UUID bookId, String title, String author, int borrowCount) {
            this.bookId = bookId;
            this.title = title;
            this.author = author;
            this.borrowCount = borrowCount;
        }
        
        public java.util.UUID getBookId() { return bookId; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public int getBorrowCount() { return borrowCount; }
    }
    
    public static class BorrowingTrendsReport {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private int totalBorrows;
        private int totalReturns;
        private int currentlyBorrowed;
        
        public BorrowingTrendsReport(LocalDateTime startDate, LocalDateTime endDate, 
                                    int totalBorrows, int totalReturns, int currentlyBorrowed) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalBorrows = totalBorrows;
            this.totalReturns = totalReturns;
            this.currentlyBorrowed = currentlyBorrowed;
        }
        
        public LocalDateTime getStartDate() { return startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public int getTotalBorrows() { return totalBorrows; }
        public int getTotalReturns() { return totalReturns; }
        public int getCurrentlyBorrowed() { return currentlyBorrowed; }
    }
    
    public static class UserActivityReport {
        private java.util.UUID userId;
        private String userName;
        private String userEmail;
        private int activityCount;
        
        public UserActivityReport(java.util.UUID userId, String userName, String userEmail, int activityCount) {
            this.userId = userId;
            this.userName = userName;
            this.userEmail = userEmail;
            this.activityCount = activityCount;
        }
        
        public java.util.UUID getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getUserEmail() { return userEmail; }
        public int getActivityCount() { return activityCount; }
    }
    
    public static class LibraryStatistics {
        private int totalBooks;
        private int availableBooks;
        private int totalUsers;
        private int activeBorrows;
        private int pendingRequests;
        private int overdueItems;
        
        public LibraryStatistics(int totalBooks, int availableBooks, int totalUsers,
                               int activeBorrows, int pendingRequests, int overdueItems) {
            this.totalBooks = totalBooks;
            this.availableBooks = availableBooks;
            this.totalUsers = totalUsers;
            this.activeBorrows = activeBorrows;
            this.pendingRequests = pendingRequests;
            this.overdueItems = overdueItems;
        }
        
        public int getTotalBooks() { return totalBooks; }
        public int getAvailableBooks() { return availableBooks; }
        public int getTotalUsers() { return totalUsers; }
        public int getActiveBorrows() { return activeBorrows; }
        public int getPendingRequests() { return pendingRequests; }
        public int getOverdueItems() { return overdueItems; }
    }
    
    public static class AuthorDistribution {
        private String author;
        private int count;
        
        public AuthorDistribution(String author, int count) {
            this.author = author;
            this.count = count;
        }
        
        public String getAuthor() { return author; }
        public int getCount() { return count; }
    }
}
