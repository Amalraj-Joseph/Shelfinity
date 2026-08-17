/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.books;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service for bulk uploading books from CSV files.
 */
@ApplicationScoped
public class BulkUploadService {
    
    private static final Logger LOGGER = Logger.getLogger(BulkUploadService.class.getName());
    
    @Inject
    private BookRepository bookRepository;
    
    /**
     * Upload books from CSV file.
     * Expected CSV format: title,author,isbn,description,totalCopies
     */
    @Transactional
    public BulkUploadResult uploadFromCSV(InputStream inputStream) {
        List<String> successMessages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        int lineNumber = 0;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    if (line.toLowerCase().contains("title") && line.toLowerCase().contains("author")) {
                        continue; // Skip header
                    }
                }
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    Book book = parseCSVLine(line, lineNumber);
                    
                    // Check if book with same ISBN already exists
                    if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                        if (bookRepository.findByIsbn(book.getIsbn()).isPresent()) {
                            errorCount++;
                            errorMessages.add("Line " + lineNumber + ": Book with ISBN " + book.getIsbn() + " already exists");
                            continue;
                        }
                    }
                    
                    bookRepository.save(book);
                    successCount++;
                    successMessages.add("Line " + lineNumber + ": Successfully added '" + book.getTitle() + "'");
                    
                } catch (Exception e) {
                    errorCount++;
                    errorMessages.add("Line " + lineNumber + ": " + e.getMessage());
                    LOGGER.warning("Error processing line " + lineNumber + ": " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            LOGGER.severe("Error reading CSV file: " + e.getMessage());
            errorMessages.add("Error reading file: " + e.getMessage());
        }
        
        return new BulkUploadResult(successCount, errorCount, successMessages, errorMessages);
    }
    
    /**
     * Parse a single CSV line into a Book object.
     * Expected format: title,author,isbn,description,totalCopies
     */
    private Book parseCSVLine(String line, int lineNumber) {
        // Split by comma, but handle quoted fields
        List<String> fields = parseCSVFields(line);
        
        if (fields.size() < 2) {
            throw new IllegalArgumentException("Invalid CSV format. Expected at least title and author");
        }
        
        String title = fields.get(0).trim();
        String author = fields.get(1).trim();
        String isbn = fields.size() > 2 ? fields.get(2).trim() : "";
        String description = fields.size() > 3 ? fields.get(3).trim() : "";
        int totalCopies = 1;
        
        if (fields.size() > 4) {
            try {
                totalCopies = Integer.parseInt(fields.get(4).trim());
                if (totalCopies < 1) {
                    totalCopies = 1;
                }
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid totalCopies value on line " + lineNumber + ", using default: 1");
            }
        }
        
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        
        if (author.isEmpty()) {
            throw new IllegalArgumentException("Author cannot be empty");
        }
        
        Book book = new Book(title, author, isbn, description, totalCopies);
        return book;
    }
    
    /**
     * Parse CSV fields, handling quoted fields with commas.
     */
    private List<String> parseCSVFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // Add the last field
        fields.add(currentField.toString());
        
        return fields;
    }
    
    /**
     * Generate a sample CSV template.
     */
    public String generateCSVTemplate() {
        return "title,author,isbn,description,totalCopies\n" +
               "\"Sample Book Title\",\"Sample Author\",\"978-0-123456-78-9\",\"A sample book description\",1\n" +
               "\"Another Book\",\"Another Author\",\"978-0-987654-32-1\",\"Another description\",2";
    }
    
    /**
     * Result of bulk upload operation.
     */
    public static class BulkUploadResult {
        private final int successCount;
        private final int errorCount;
        private final List<String> successMessages;
        private final List<String> errorMessages;
        
        public BulkUploadResult(int successCount, int errorCount, 
                               List<String> successMessages, List<String> errorMessages) {
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.successMessages = successMessages;
            this.errorMessages = errorMessages;
        }
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public int getErrorCount() {
            return errorCount;
        }
        
        public List<String> getSuccessMessages() {
            return successMessages;
        }
        
        public List<String> getErrorMessages() {
            return errorMessages;
        }
        
        public int getTotalProcessed() {
            return successCount + errorCount;
        }
    }
}
