/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.overdue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.books.Book;
import com.shelfinity.books.BookRepository;
import com.shelfinity.queues.QueueItem;
import com.shelfinity.queues.QueueType;
import com.shelfinity.queues.dto.responses.QueueItemResponse;
import com.shelfinity.security.JwtUtil;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;

import java.util.UUID;

import jakarta.ws.rs.core.Response;

/**
 * Container-managed @RolesAllowed("admin") isn't exercised outside a real
 * deployment; these tests cover the manual isAuthenticated() gate and
 * successful delegation to OverdueService, which is what's actually testable
 * at this layer without a running server.
 */
@ExtendWith(MockitoExtension.class)
class OverdueResourceTest {

    @Mock private OverdueService overdueService;
    @Mock private JwtUtil jwtUtil;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private OverdueResource overdueResource;

    @Test
    void getAllOverdueItems_notAuthenticated_returns401() {
        when(jwtUtil.isAuthenticated()).thenReturn(false);

        Response response = overdueResource.getAllOverdueItems();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getAllOverdueItems_authenticated_delegatesToService() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        when(overdueService.getOverdueItems()).thenReturn(List.of());

        Response response = overdueResource.getAllOverdueItems();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    // Overdue items previously serialized raw QueueItem entities (UUIDs
    // only); they must now resolve to a human-readable book title/ISBN and
    // user name/email, matching the rest of the admin UI's identifiers.
    @Test
    void getAllOverdueItems_resolvesBookAndUserForDisplay() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        UUID bookId = UUID.randomUUID();
        QueueItem item = new QueueItem(QueueType.BOOK_BORROW, "kc-1", bookId, "desc");
        when(overdueService.getOverdueItems()).thenReturn(List.of(item));

        Book book = new Book();
        book.setTitle("Clean Code");
        book.setIsbn("978-0132350884");
        when(bookRepository.findById(bookId)).thenReturn(java.util.Optional.of(book));
        when(userRepository.findByKeycloakId("kc-1"))
                .thenReturn(java.util.Optional.of(new User("kc-1", "alice@shelfinity.com", "Alice")));

        Response response = overdueResource.getAllOverdueItems();

        @SuppressWarnings("unchecked")
        List<QueueItemResponse> body = (List<QueueItemResponse>) response.getEntity();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).getBookTitle()).isEqualTo("Clean Code");
        assertThat(body.get(0).getUserName()).isEqualTo("Alice");
    }

    @Test
    void getMyOverdueItems_notAuthenticated_returns401() {
        when(jwtUtil.isAuthenticated()).thenReturn(false);

        Response response = overdueResource.getMyOverdueItems();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getMyOverdueItems_authenticated_usesCallerKeycloakId() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        when(jwtUtil.getCurrentUserKeycloakId()).thenReturn(java.util.Optional.of("kc-1"));
        when(overdueService.getOverdueItemsForUser("kc-1")).thenReturn(List.of());

        Response response = overdueResource.getMyOverdueItems();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void getOverdueStats_notAuthenticated_returns401() {
        when(jwtUtil.isAuthenticated()).thenReturn(false);

        Response response = overdueResource.getOverdueStats();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getOverdueStats_authenticated_returnsStats() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        when(overdueService.getOverdueStats()).thenReturn(new OverdueService.OverdueStats(0, 0, 0));

        Response response = overdueResource.getOverdueStats();

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
