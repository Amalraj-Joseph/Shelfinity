/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.reports.ReportService.LibraryStatistics;
import com.shelfinity.security.JwtUtil;

import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class ReportResourceTest {

    @Mock private ReportService reportService;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private ReportResource reportResource;

    @Test
    void getBookPopularityReport_notAuthenticated_returns401() {
        when(jwtUtil.isAuthenticated()).thenReturn(false);

        Response response = reportResource.getBookPopularityReport(10);

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getBookPopularityReport_authenticated_passesLimitThrough() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        when(reportService.getBookPopularityReport(5)).thenReturn(List.of());

        Response response = reportResource.getBookPopularityReport(5);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(reportService).getBookPopularityReport(5);
    }

    @Test
    void getBorrowingTrends_authenticated_computesDateWindow() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        when(reportService.getBorrowingTrends(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new ReportService.BorrowingTrendsReport(null, null, 0, 0, 0));

        Response response = reportResource.getBorrowingTrends(7);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void getUserActivityReport_notAuthenticated_returns401() {
        when(jwtUtil.isAuthenticated()).thenReturn(false);

        Response response = reportResource.getUserActivityReport(10);

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getLibraryStatistics_authenticated_returnsStats() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        when(reportService.getLibraryStatistics())
                .thenReturn(new LibraryStatistics(10, 5, 3, 2, 1, 0));

        Response response = reportResource.getLibraryStatistics();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(LibraryStatistics.class);
    }

    @Test
    void getAuthorDistribution_notAuthenticated_returns401() {
        when(jwtUtil.isAuthenticated()).thenReturn(false);

        Response response = reportResource.getAuthorDistribution();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getAuthorDistribution_authenticated_delegatesToService() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        when(reportService.getAuthorDistribution()).thenReturn(List.of());

        Response response = reportResource.getAuthorDistribution();

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
