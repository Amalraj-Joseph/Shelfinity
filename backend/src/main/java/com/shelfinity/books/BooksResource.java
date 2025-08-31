/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.books;

import com.shelfinity.books.dto.requests.CreateBookRequest;
import com.shelfinity.books.dto.responses.BookResponse;
import com.shelfinity.security.JwtUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST API for book management.
 */
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class BooksResource {
    
    @Inject
    private BookRepository bookRepository;
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Context
    private HttpHeaders headers;
    
    /**
     * Create a new book (admin only).
     */
    @POST
    public Response createBook(@Valid CreateBookRequest request) {
        // Verify admin access
        Optional<JwtUtil.UserInfo> userInfo = getCurrentUser();
        if (userInfo.isEmpty() || !"ADMIN".equals(userInfo.get().getRole())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        // Check if book with same ISBN already exists
        if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty() && 
            bookRepository.existsByIsbn(request.getIsbn())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Book with this ISBN already exists\"}")
                    .build();
        }
        
        // Create new book
        Book book = new Book(request.getTitle(), request.getAuthor(), 
                           request.getIsbn(), request.getDescription(), request.getTotalCopies());
        
        Book savedBook = bookRepository.save(book);
        BookResponse response = new BookResponse(savedBook);
        
        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }
    
    /**
     * Get all books.
     */
    @GET
    public Response getAllBooks(@QueryParam("search") String search,
                               @QueryParam("available") Boolean available) {
        List<Book> books;
        
        if (search != null && !search.trim().isEmpty()) {
            books = bookRepository.search(search.trim());
        } else if (available != null && available) {
            books = bookRepository.findAvailable();
        } else {
            books = bookRepository.findAll();
        }
        
        List<BookResponse> responses = books.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return Response.ok(responses).build();
    }
    
    /**
     * Get book by ID.
     */
    @GET
    @Path("/{id}")
    public Response getBookById(@PathParam("id") String id) {
        try {
            UUID bookId = UUID.fromString(id);
            Optional<Book> book = bookRepository.findById(bookId);
            
            if (book.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Book not found\"}")
                        .build();
            }
            
            BookResponse response = new BookResponse(book.get());
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid book ID format\"}")
                    .build();
        }
    }
    
    /**
     * Update book by ID (admin only).
     */
    @PUT
    @Path("/{id}")
    public Response updateBook(@PathParam("id") String id, @Valid CreateBookRequest request) {
        // Verify admin access
        Optional<JwtUtil.UserInfo> userInfo = getCurrentUser();
        if (userInfo.isEmpty() || !"ADMIN".equals(userInfo.get().getRole())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        try {
            UUID bookId = UUID.fromString(id);
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            
            if (bookOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Book not found\"}")
                        .build();
            }
            
            Book book = bookOpt.get();
            book.setTitle(request.getTitle());
            book.setAuthor(request.getAuthor());
            book.setIsbn(request.getIsbn());
            book.setDescription(request.getDescription());
            book.setTotalCopies(request.getTotalCopies());
            
            Book updatedBook = bookRepository.update(book);
            BookResponse response = new BookResponse(updatedBook);
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid book ID format\"}")
                    .build();
        }
    }
    
    /**
     * Delete book by ID (admin only).
     */
    @DELETE
    @Path("/{id}")
    public Response deleteBook(@PathParam("id") String id) {
        // Verify admin access
        Optional<JwtUtil.UserInfo> userInfo = getCurrentUser();
        if (userInfo.isEmpty() || !"ADMIN".equals(userInfo.get().getRole())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        try {
            UUID bookId = UUID.fromString(id);
            Optional<Book> book = bookRepository.findById(bookId);
            
            if (book.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Book not found\"}")
                        .build();
            }
            
            bookRepository.deleteById(bookId);
            return Response.noContent().build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid book ID format\"}")
                    .build();
        }
    }
    
    /**
     * Search books by title or author.
     */
    @GET
    @Path("/search")
    public Response searchBooks(@QueryParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Search query is required\"}")
                    .build();
        }
        
        List<Book> books = bookRepository.search(query.trim());
        List<BookResponse> responses = books.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return Response.ok(responses).build();
    }
    
    /**
     * Get available books only.
     */
    @GET
    @Path("/available")
    public Response getAvailableBooks() {
        List<Book> books = bookRepository.findAvailable();
        List<BookResponse> responses = books.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return Response.ok(responses).build();
    }
    
    /**
     * Get current user from JWT token.
     */
    private Optional<JwtUtil.UserInfo> getCurrentUser() {
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            return Optional.empty();
        }
        
        Optional<String> token = jwtUtil.extractTokenFromHeader(authHeader);
        if (token.isEmpty()) {
            return Optional.empty();
        }
        
        return jwtUtil.extractUserInfo(token.get());
    }
}
