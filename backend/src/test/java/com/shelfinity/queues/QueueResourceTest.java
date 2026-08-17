/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.books.Book;
import com.shelfinity.books.BookRepository;
import com.shelfinity.queues.dto.requests.CreateQueueItemRequest;
import com.shelfinity.queues.dto.requests.UpdateQueueItemRequest;
import com.shelfinity.queues.dto.responses.QueueItemResponse;
import com.shelfinity.security.JwtUtil;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;

import jakarta.ws.rs.core.Response;

/**
 * SPEC.md §6.1 step 3 (approval gate on transacting), the duplicate-pending
 * guard, and the §10.2 approval-delegation/exception-mapping in
 * {@link QueueResource}.
 */
@ExtendWith(MockitoExtension.class)
class QueueResourceTest {

    @Mock private QueueRepository queueRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserRepository userRepository;
    @Mock private BookRepository bookRepository;
    @Mock private QueueApprovalService queueApprovalService;

    @InjectMocks
    private QueueResource queueResource;

    private static JwtUtil.UserInfo userInfo(String keycloakId) {
        JwtUtil.UserInfo info = new JwtUtil.UserInfo();
        info.setKeycloakId(keycloakId);
        return info;
    }

    private static CreateQueueItemRequest borrowRequest(UUID bookId) {
        CreateQueueItemRequest req = new CreateQueueItemRequest();
        req.setType(QueueType.BOOK_BORROW);
        req.setBookId(bookId);
        req.setDescription("I'd like to borrow this");
        return req;
    }

    // ---- createQueueItem: approval gate ---------------------------------

    @Test
    void createQueueItem_unauthenticated_returns401() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.empty());

        Response response = queueResource.createQueueItem(borrowRequest(UUID.randomUUID()));

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void createQueueItem_inactiveAccount_returns403PendingApproval() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        User inactive = new User("kc-1", "a@b.com", "Alice");
        inactive.setActive(false);
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(inactive));

        Response response = queueResource.createQueueItem(borrowRequest(UUID.randomUUID()));

        assertThat(response.getStatus()).isEqualTo(403);
        verify(queueRepository, never()).save(any());
    }

    @Test
    void createQueueItem_noLocalUserRecordYet_returns403PendingApproval() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.empty());

        Response response = queueResource.createQueueItem(borrowRequest(UUID.randomUUID()));

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void createQueueItem_activeAccountBorrowMissingBookId_returns400() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        User active = new User("kc-1", "a@b.com", "Alice");
        active.setActive(true);
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(active));

        Response response = queueResource.createQueueItem(borrowRequest(null));

        assertThat(response.getStatus()).isEqualTo(400);
        verify(queueRepository, never()).save(any());
    }

    @Test
    void createQueueItem_duplicatePendingRequest_returns409() {
        UUID bookId = UUID.randomUUID();
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        User active = new User("kc-1", "a@b.com", "Alice");
        active.setActive(true);
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(active));
        when(queueRepository.existsPendingForUserAndBook("kc-1", bookId, QueueType.BOOK_BORROW))
                .thenReturn(true);

        Response response = queueResource.createQueueItem(borrowRequest(bookId));

        assertThat(response.getStatus()).isEqualTo(409);
        verify(queueRepository, never()).save(any());
    }

    @Test
    void createQueueItem_activeAccountValidBorrow_createsPendingItem() {
        UUID bookId = UUID.randomUUID();
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        User active = new User("kc-1", "a@b.com", "Alice");
        active.setActive(true);
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(active));
        when(queueRepository.existsPendingForUserAndBook("kc-1", bookId, QueueType.BOOK_BORROW))
                .thenReturn(false);
        when(queueRepository.save(any(QueueItem.class))).thenAnswer(inv -> inv.getArgument(0));

        Response response = queueResource.createQueueItem(borrowRequest(bookId));

        assertThat(response.getStatus()).isEqualTo(201);
        ArgumentCaptor<QueueItem> captor = ArgumentCaptor.forClass(QueueItem.class);
        verify(queueRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(QueueStatus.PENDING);
        assertThat(captor.getValue().getUserKeycloakId()).isEqualTo("kc-1");
    }

    // ---- updateQueueItemStatus: approval delegation ----------------------

    @Test
    void updateQueueItemStatus_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = queueResource.updateQueueItemStatus(
                UUID.randomUUID().toString(), new UpdateQueueItemRequest(QueueStatus.APPROVED));

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void updateQueueItemStatus_approvalSucceeds_persistsApprovedStatus() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        UUID itemId = UUID.randomUUID();
        QueueItem item = new QueueItem(QueueType.BOOK_BORROW, "kc-1", UUID.randomUUID(), "desc");
        item.setStatus(QueueStatus.PENDING);
        when(queueRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(queueRepository.update(any(QueueItem.class))).thenAnswer(inv -> inv.getArgument(0));

        Response response = queueResource.updateQueueItemStatus(
                itemId.toString(), new UpdateQueueItemRequest(QueueStatus.APPROVED));

        assertThat(response.getStatus()).isEqualTo(200);
        verify(queueApprovalService).applyApproval(item);
        assertThat(item.getStatus()).isEqualTo(QueueStatus.APPROVED);
    }

    @Test
    void updateQueueItemStatus_approvalThrows_mapsExceptionStatusAndDoesNotPersist() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        UUID itemId = UUID.randomUUID();
        QueueItem item = new QueueItem(QueueType.BOOK_BORROW, "kc-1", UUID.randomUUID(), "desc");
        item.setStatus(QueueStatus.PENDING);
        when(queueRepository.findById(itemId)).thenReturn(Optional.of(item));
        doThrow(new QueueApprovalException(Response.Status.CONFLICT, "No copies of this book are currently available"))
                .when(queueApprovalService).applyApproval(item);

        Response response = queueResource.updateQueueItemStatus(
                itemId.toString(), new UpdateQueueItemRequest(QueueStatus.APPROVED));

        assertThat(response.getStatus()).isEqualTo(409);
        verify(queueRepository, never()).update(any());
        assertThat(item.getStatus()).isEqualTo(QueueStatus.PENDING);
    }

    @Test
    void updateQueueItemStatus_rejection_notifiesButDoesNotCallApproval() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        UUID itemId = UUID.randomUUID();
        QueueItem item = new QueueItem(QueueType.BOOK_BORROW, "kc-1", UUID.randomUUID(), "desc");
        item.setStatus(QueueStatus.PENDING);
        when(queueRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(queueRepository.update(any(QueueItem.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateQueueItemRequest rejectRequest = new UpdateQueueItemRequest(QueueStatus.REJECTED, "Not eligible");
        Response response = queueResource.updateQueueItemStatus(itemId.toString(), rejectRequest);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(queueApprovalService, never()).applyApproval(any());
        verify(queueApprovalService).notifyRejection(item, "Not eligible");
        assertThat(item.getStatus()).isEqualTo(QueueStatus.REJECTED);
    }

    @Test
    void updateQueueItemStatus_reprocessingAlreadyApprovedItem_doesNotReapplySideEffects() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        UUID itemId = UUID.randomUUID();
        QueueItem item = new QueueItem(QueueType.BOOK_BORROW, "kc-1", UUID.randomUUID(), "desc");
        item.setStatus(QueueStatus.APPROVED); // already processed
        when(queueRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(queueRepository.update(any(QueueItem.class))).thenAnswer(inv -> inv.getArgument(0));

        queueResource.updateQueueItemStatus(itemId.toString(), new UpdateQueueItemRequest(QueueStatus.APPROVED));

        verify(queueApprovalService, never()).applyApproval(any());
    }

    @Test
    void updateQueueItemStatus_itemNotFound_returns404() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        UUID itemId = UUID.randomUUID();
        when(queueRepository.findById(itemId)).thenReturn(Optional.empty());

        Response response = queueResource.updateQueueItemStatus(
                itemId.toString(), new UpdateQueueItemRequest(QueueStatus.APPROVED));

        assertThat(response.getStatus()).isEqualTo(404);
    }

    // ---- getAllQueueItems ------------------------------------------------

    @Test
    void getAllQueueItems_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = queueResource.getAllQueueItems(null, null);

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void getAllQueueItems_statusFilter_delegatesToFindByStatus() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(queueRepository.findByStatus(QueueStatus.PENDING)).thenReturn(List.of());

        Response response = queueResource.getAllQueueItems("pending", null);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(queueRepository).findByStatus(QueueStatus.PENDING);
    }

    @Test
    void getAllQueueItems_invalidStatus_returns400() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);

        Response response = queueResource.getAllQueueItems("not-a-status", null);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void getAllQueueItems_typeFilter_delegatesToFindByType() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(queueRepository.findByType(QueueType.BOOK_BORROW)).thenReturn(List.of());

        Response response = queueResource.getAllQueueItems(null, "book_borrow");

        assertThat(response.getStatus()).isEqualTo(200);
        verify(queueRepository).findByType(QueueType.BOOK_BORROW);
    }

    @Test
    void getAllQueueItems_invalidType_returns400() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);

        Response response = queueResource.getAllQueueItems(null, "not-a-type");

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void getAllQueueItems_noFilters_returnsAll() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(queueRepository.findAll()).thenReturn(List.of());

        Response response = queueResource.getAllQueueItems(null, null);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(queueRepository).findAll();
    }

    // ---- getQueueItemById --------------------------------------------------

    @Test
    void getQueueItemById_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(queueRepository.findById(id)).thenReturn(Optional.empty());

        Response response = queueResource.getQueueItemById(id.toString());

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void getQueueItemById_invalidUuid_returns400() {
        Response response = queueResource.getQueueItemById("not-a-uuid");

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void getQueueItemById_found_returns200() {
        UUID id = UUID.randomUUID();
        QueueItem item = new QueueItem(QueueType.BOOK_BORROW, "kc-1", UUID.randomUUID(), "desc");
        when(queueRepository.findById(id)).thenReturn(Optional.of(item));

        Response response = queueResource.getQueueItemById(id.toString());

        assertThat(response.getStatus()).isEqualTo(200);
    }

    // UI identifiers must be human-readable (book title/ISBN, user name/email)
    // alongside the raw UUIDs, not raw UUIDs alone — see QueueItemResponse.
    @Test
    void getQueueItemById_resolvesBookAndUserForDisplay() {
        UUID id = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        QueueItem item = new QueueItem(QueueType.BOOK_BORROW, "kc-1", bookId, "desc");
        when(queueRepository.findById(id)).thenReturn(Optional.of(item));

        Book book = new Book();
        book.setTitle("Clean Code");
        book.setIsbn("978-0132350884");
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        User user = new User("kc-1", "alice@shelfinity.com", "Alice");
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(user));

        Response response = queueResource.getQueueItemById(id.toString());

        QueueItemResponse body = (QueueItemResponse) response.getEntity();
        assertThat(body.getBookTitle()).isEqualTo("Clean Code");
        assertThat(body.getBookIsbn()).isEqualTo("978-0132350884");
        assertThat(body.getUserName()).isEqualTo("Alice");
        assertThat(body.getUserEmail()).isEqualTo("alice@shelfinity.com");
    }

    // ---- getMyQueueItems ----------------------------------------------------

    @Test
    void getMyQueueItems_notAuthenticated_returns401() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.empty());

        Response response = queueResource.getMyQueueItems();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getMyQueueItems_authenticated_scopesToCaller() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(queueRepository.findByUserKeycloakId("kc-1")).thenReturn(List.of());

        Response response = queueResource.getMyQueueItems();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(queueRepository).findByUserKeycloakId("kc-1");
    }

    // ---- deleteQueueItem ------------------------------------------------

    @Test
    void deleteQueueItem_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = queueResource.deleteQueueItem(UUID.randomUUID().toString());

        assertThat(response.getStatus()).isEqualTo(403);
        verify(queueRepository, never()).deleteById(any());
    }

    @Test
    void deleteQueueItem_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(queueRepository.findById(id)).thenReturn(Optional.empty());

        Response response = queueResource.deleteQueueItem(id.toString());

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void deleteQueueItem_admin_deletesAndReturns204() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(queueRepository.findById(id))
                .thenReturn(Optional.of(new QueueItem(QueueType.BOOK_BORROW, "kc-1", UUID.randomUUID(), "desc")));

        Response response = queueResource.deleteQueueItem(id.toString());

        assertThat(response.getStatus()).isEqualTo(204);
        verify(queueRepository).deleteById(id);
    }
}
