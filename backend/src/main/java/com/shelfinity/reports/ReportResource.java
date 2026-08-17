/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reports;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.shelfinity.reports.ReportService.AuthorDistribution;
import com.shelfinity.reports.ReportService.BookPopularityReport;
import com.shelfinity.reports.ReportService.BorrowingTrendsReport;
import com.shelfinity.reports.ReportService.LibraryStatistics;
import com.shelfinity.reports.ReportService.UserActivityReport;
import com.shelfinity.security.JwtUtil;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API for library reports and analytics.
 */
@Path("/reports")
@Tag(name = "Reports", description = "Library reports and analytics operations")
public class ReportResource {
    
    private static final Logger LOGGER = Logger.getLogger(ReportResource.class.getName());
    
    @Inject
    private ReportService reportService;
    
    @Inject
    private JwtUtil jwtUtil;
    
    /**
     * Get book popularity report (admin only).
     */
    @GET
    @Path("/book-popularity")
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get book popularity report", description = "Get most borrowed books (admin only)")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Book popularity report retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = BookPopularityReport.class)
            )
        ),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden - Admin access required"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getBookPopularityReport(
            @Parameter(description = "Maximum number of books to return") 
            @QueryParam("limit") @DefaultValue("10") int limit) {
        try {
            if (!jwtUtil.isAuthenticated()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Not authenticated\"}")
                    .build();
            }
            
            List<BookPopularityReport> report = reportService.getBookPopularityReport(limit);
            return Response.ok(report).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error generating book popularity report: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to generate report\"}")
                .build();
        }
    }
    
    /**
     * Get borrowing trends report (admin only).
     */
    @GET
    @Path("/borrowing-trends")
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get borrowing trends", description = "Get borrowing trends over a time period (admin only)")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Borrowing trends report retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = BorrowingTrendsReport.class)
            )
        ),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden - Admin access required"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getBorrowingTrends(
            @Parameter(description = "Number of days to look back") 
            @QueryParam("days") @DefaultValue("30") int days) {
        try {
            if (!jwtUtil.isAuthenticated()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Not authenticated\"}")
                    .build();
            }
            
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            
            BorrowingTrendsReport report = reportService.getBorrowingTrends(startDate, endDate);
            return Response.ok(report).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error generating borrowing trends report: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to generate report\"}")
                .build();
        }
    }
    
    /**
     * Get user activity report (admin only).
     */
    @GET
    @Path("/user-activity")
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get user activity report", description = "Get most active users (admin only)")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User activity report retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserActivityReport.class)
            )
        ),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden - Admin access required"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getUserActivityReport(
            @Parameter(description = "Maximum number of users to return") 
            @QueryParam("limit") @DefaultValue("10") int limit) {
        try {
            if (!jwtUtil.isAuthenticated()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Not authenticated\"}")
                    .build();
            }
            
            List<UserActivityReport> report = reportService.getUserActivityReport(limit);
            return Response.ok(report).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error generating user activity report: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to generate report\"}")
                .build();
        }
    }
    
    /**
     * Get library statistics (admin only).
     */
    @GET
    @Path("/statistics")
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get library statistics", description = "Get overall library statistics (admin only)")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Library statistics retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = LibraryStatistics.class)
            )
        ),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden - Admin access required"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getLibraryStatistics() {
        try {
            if (!jwtUtil.isAuthenticated()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Not authenticated\"}")
                    .build();
            }
            
            LibraryStatistics stats = reportService.getLibraryStatistics();
            return Response.ok(stats).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error generating library statistics: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to generate statistics\"}")
                .build();
        }
    }
    
    /**
     * Get author distribution report (admin only).
     */
    @GET
    @Path("/author-distribution")
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get author distribution", description = "Get distribution of books by author (admin only)")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Author distribution retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = AuthorDistribution.class)
            )
        ),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden - Admin access required"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAuthorDistribution() {
        try {
            if (!jwtUtil.isAuthenticated()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Not authenticated\"}")
                    .build();
            }
            
            List<AuthorDistribution> distribution = reportService.getAuthorDistribution();
            return Response.ok(distribution).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error generating author distribution: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to generate distribution\"}")
                .build();
        }
    }
}
