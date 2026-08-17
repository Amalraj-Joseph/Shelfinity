/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.books;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.shelfinity.books.dto.requests.CreateBookRequest;
import com.shelfinity.books.dto.responses.BookResponse;
import com.shelfinity.security.JwtUtil;

import com.shelfinity.books.BulkUploadService.BulkUploadResult;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * REST API for book management.
 */
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Tag(name = "Books")
public class BooksResource {
    
    @Inject
    private BookRepository bookRepository;
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Inject
    private BulkUploadService bulkUploadService;
    
    @Context
    private SecurityContext securityContext;

    /**
     * Create a new book (admin only).
     */
    @POST
    @Tag(name = "Books")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Create a new book",
        description = "Creates a new book in the library system. Requires admin privileges."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Book created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = BookResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Invalid book data\"}")
            )
        ),
        @APIResponse(
            responseCode = "403",
            description = "Admin access required",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Admin access required\"}")
            )
        )
    })
    public Response createBook(
        @RequestBody(
            description = "Book information to create",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CreateBookRequest.class),
                examples = @ExampleObject(
                    name = "Create Book Example",
                    value = """
                    {
                        "title": "The Great Gatsby",
                        "author": "F. Scott Fitzgerald",
                        "isbn": "978-0-679-72327-6",
                        "description": "A novel about the American Dream and the Jazz Age.",
                        "genre": "CLASSIC",
                        "publicationYear": 1925,
                        "totalCopies": 3
                    }
                    """
                )
            )
        ) @Valid CreateBookRequest request) {
        
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        // Check if book already exists
        Optional<Book> existingBook = bookRepository.findByIsbn(request.getIsbn());
        if (existingBook.isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Book with this ISBN already exists\"}")
                    .build();
        }
        
        // Create new book
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setGenre(request.getGenre());
        book.setPublicationYear(request.getPublicationYear());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(request.getTotalCopies());

        Book savedBook = bookRepository.save(book);
        BookResponse response = new BookResponse(savedBook);
        
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
    
    /**
     * Get all books.
     */
    @GET
    @Tag(name = "Books")
    @Operation(
        summary = "Get all books",
        description = "Retrieves a list of all books in the library system."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Books retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = BookResponse.class)
            )
        )
    })
    public Response getAllBooks(@QueryParam("availableOnly") Boolean availableOnly,
                                 @QueryParam("genre") String genre) {
        List<Book> books;
        if (genre != null && !genre.trim().isEmpty()) {
            books = bookRepository.findByGenre(genre.trim());
        } else if (availableOnly != null && availableOnly) {
            books = bookRepository.findAvailable();
        } else {
            books = bookRepository.findAll();
        }

        if (genre != null && !genre.trim().isEmpty() && availableOnly != null && availableOnly) {
            books = books.stream().filter(Book::isAvailable).collect(Collectors.toList());
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
    @Tag(name = "Books")
    @Operation(
        summary = "Get book by ID",
        description = "Retrieves a specific book by its ID."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Book retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = BookResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Book not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Book not found\"}")
            )
        )
    })
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
    @Tag(name = "Books")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Update book by ID",
        description = "Updates a specific book by its ID. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Book updated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = BookResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "403",
            description = "Admin access required",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Admin access required\"}")
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Book not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Book not found\"}")
            )
        )
    })
    public Response updateBook(@PathParam("id") String id, @Valid CreateBookRequest request) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
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
            book.setGenre(request.getGenre());
            book.setPublicationYear(request.getPublicationYear());
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
    @Tag(name = "Books")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Delete book by ID",
        description = "Deletes a specific book by its ID. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "Book deleted successfully"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Admin access required",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Admin access required\"}")
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Book not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Book not found\"}")
            )
        )
    })
    public Response deleteBook(@PathParam("id") String id) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
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
    @Tag(name = "Books")
    @Operation(
        summary = "Search books",
        description = "Searches for books by title or author."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Search completed successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = BookResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Search query is required",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Search query is required\"}")
            )
        )
    })
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
     * Bulk upload books from CSV file (admin only).
     */
    @POST
    @Path("/bulk-upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Tag(name = "Books")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Bulk upload books from CSV",
        description = "Upload multiple books at once from a CSV file. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Bulk upload completed",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = BulkUploadResult.class)
            )
        ),
        @APIResponse(
            responseCode = "403",
            description = "Admin access required",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Admin access required\"}")
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid file format",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Invalid file format\"}")
            )
        )
    })
    public Response bulkUploadBooks(java.io.InputStream fileInputStream) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        try {
            if (fileInputStream == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"No file provided\"}")
                        .build();
            }
            
            BulkUploadResult result = bulkUploadService.uploadFromCSV(fileInputStream);
            return Response.ok(result).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to process file: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    /**
     * Download CSV template for bulk upload.
     */
    @GET
    @Path("/bulk-upload/template")
    @Produces("text/csv")
    @Tag(name = "Books")
    @Operation(
        summary = "Download CSV template",
        description = "Download a CSV template file for bulk book upload."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Template downloaded successfully",
            content = @Content(mediaType = "text/csv")
        )
    })
    public Response downloadCSVTemplate() {
        String template = bulkUploadService.generateCSVTemplate();
        return Response.ok(template)
                .header("Content-Disposition", "attachment; filename=\"books_template.csv\"")
                .build();
    }
    
    
    /**
     * Get available books only.
     */
    @GET
    @Path("/available")
    @Tag(name = "Books")
    @Operation(
        summary = "Get available books",
        description = "Retrieves a list of books that are currently available for borrowing."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Available books retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = BookResponse.class)
            )
        )
    })
    public Response getAvailableBooks() {
        List<Book> books = bookRepository.findAvailable();
        List<BookResponse> responses = books.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return Response.ok(responses).build();
    }
}
