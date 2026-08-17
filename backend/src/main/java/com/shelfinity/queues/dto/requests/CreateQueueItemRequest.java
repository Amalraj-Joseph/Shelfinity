/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues.dto.requests;

import com.shelfinity.queues.QueueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for creating a new queue item.
 *
 * Deliberately has no userKeycloakId field: QueueResource.createQueueItem()
 * always derives the requester's identity from the authenticated JWT and
 * never trusts client-supplied identity (this field previously existed with
 * a @NotBlank constraint that nothing ever populated or read — every real
 * request omits it, since a caller shouldn't be asserting whose request this
 * is — which meant bean validation rejected every legitimate call with a 400
 * before the resource method ever ran. Found via an end-to-end smoke test
 * through the real running stack; no unit or mock-based test caught it
 * because none of them exercise the actual Bean Validation interceptor.
 */
public class CreateQueueItemRequest {

    @NotNull(message = "Queue type is required")
    private QueueType type;

    private UUID bookId;

    @NotBlank(message = "Description is required")
    private String description;

    public CreateQueueItemRequest() {}

    public CreateQueueItemRequest(QueueType type, UUID bookId, String description) {
        this.type = type;
        this.bookId = bookId;
        this.description = description;
    }

    public QueueType getType() {
        return type;
    }

    public void setType(QueueType type) {
        this.type = type;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CreateQueueItemRequest{" +
                "type=" + type +
                ", bookId=" + bookId +
                ", description='" + description + '\'' +
                '}';
    }
}
