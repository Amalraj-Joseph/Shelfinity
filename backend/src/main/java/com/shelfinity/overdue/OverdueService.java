/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.overdue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import com.shelfinity.books.Book;
import com.shelfinity.books.BookRepository;
import com.shelfinity.email.EmailService;
import com.shelfinity.queues.QueueItem;
import com.shelfinity.queues.QueueRepository;
import com.shelfinity.queues.QueueStatus;
import com.shelfinity.queues.QueueType;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

/**
 * Service for tracking and managing overdue books.
 * Runs scheduled jobs to check for overdue items and send notifications.
 */
@Singleton
public class OverdueService {
    
    private static final Logger LOGGER = Logger.getLogger(OverdueService.class.getName());
    
    @Inject
    private QueueRepository queueRepository;
    
    @Inject
    private BookRepository bookRepository;
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private EmailService emailService;
    
    /**
     * Scheduled job that runs daily at 9 AM to check for overdue books.
     * Sends email notifications to users with overdue books.
     */
    @Schedule(hour = "9", minute = "0", persistent = false)
    public void checkOverdueBooks() {
        LOGGER.info("Starting overdue books check...");
        
        try {
            List<QueueItem> overdueItems = queueRepository.findOverdueItems();
            LOGGER.info("Found " + overdueItems.size() + " overdue items");
            
            for (QueueItem item : overdueItems) {
                processOverdueItem(item);
            }
            
            LOGGER.info("Overdue books check completed");
        } catch (Exception e) {
            LOGGER.severe("Error checking overdue books: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Process a single overdue item - send notification email.
     */
    private void processOverdueItem(QueueItem item) {
        try {
            // Get user information
            Optional<User> userOpt = userRepository.findByKeycloakId(item.getUserKeycloakId());
            if (!userOpt.isPresent()) {
                LOGGER.warning("User not found for overdue item: " + item.getId());
                return;
            }
            User user = userOpt.get();
            
            // Get book information
            Optional<Book> bookOpt = bookRepository.findById(item.getBookId());
            if (!bookOpt.isPresent()) {
                LOGGER.warning("Book not found for overdue item: " + item.getId());
                return;
            }
            Book book = bookOpt.get();
            
            // Calculate days overdue
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                item.getDueDate(), LocalDateTime.now()
            );
            
            // Send overdue notification
            emailService.sendOverdueNotification(
                user.getEmail(),
                user.getName(),
                book.getTitle(),
                item.getDueDate(),
                (int) daysOverdue
            );
            
            LOGGER.info("Sent overdue notification for book: " + book.getTitle() + 
                       " to user: " + user.getName() + " (" + daysOverdue + " days overdue)");
            
        } catch (Exception e) {
            LOGGER.severe("Error processing overdue item " + item.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get all currently overdue items.
     */
    public List<QueueItem> getOverdueItems() {
        return queueRepository.findOverdueItems();
    }
    
    /**
     * Get overdue items for a specific user.
     */
    public List<QueueItem> getOverdueItemsForUser(String userKeycloakId) {
        return queueRepository.findOverdueItemsByUser(userKeycloakId);
    }
    
    /**
     * Check if a specific queue item is overdue.
     */
    public boolean isOverdue(QueueItem item) {
        if (item.getDueDate() == null) {
            return false;
        }
        
        // Only APPROVED borrow requests can be overdue
        if (item.getType() != QueueType.BOOK_BORROW || item.getStatus() != QueueStatus.APPROVED) {
            return false;
        }
        
        return LocalDateTime.now().isAfter(item.getDueDate());
    }
    
    /**
     * Calculate the number of days a book is overdue.
     * Returns 0 if not overdue.
     */
    public long getDaysOverdue(QueueItem item) {
        if (!isOverdue(item)) {
            return 0;
        }
        
        return java.time.temporal.ChronoUnit.DAYS.between(
            item.getDueDate(), LocalDateTime.now()
        );
    }
    
    /**
     * Get overdue statistics.
     */
    public OverdueStats getOverdueStats() {
        List<QueueItem> overdueItems = queueRepository.findOverdueItems();
        
        int totalOverdue = overdueItems.size();
        long totalDaysOverdue = overdueItems.stream()
            .mapToLong(this::getDaysOverdue)
            .sum();
        
        double averageDaysOverdue = totalOverdue > 0 ? 
            (double) totalDaysOverdue / totalOverdue : 0;
        
        return new OverdueStats(totalOverdue, totalDaysOverdue, averageDaysOverdue);
    }
    
    /**
     * Inner class for overdue statistics.
     */
    public static class OverdueStats {
        private final int totalOverdueItems;
        private final long totalDaysOverdue;
        private final double averageDaysOverdue;
        
        public OverdueStats(int totalOverdueItems, long totalDaysOverdue, double averageDaysOverdue) {
            this.totalOverdueItems = totalOverdueItems;
            this.totalDaysOverdue = totalDaysOverdue;
            this.averageDaysOverdue = averageDaysOverdue;
        }
        
        public int getTotalOverdueItems() {
            return totalOverdueItems;
        }
        
        public long getTotalDaysOverdue() {
            return totalDaysOverdue;
        }
        
        public double getAverageDaysOverdue() {
            return averageDaysOverdue;
        }
    }
}

// Made with Bob
