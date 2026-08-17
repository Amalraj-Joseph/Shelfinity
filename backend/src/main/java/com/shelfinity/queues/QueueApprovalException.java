/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues;

import jakarta.ws.rs.core.Response;

/**
 * Thrown by {@link QueueApprovalService} when a PENDING -&gt; APPROVED
 * transition can't be applied (e.g. no copies available, book missing).
 * Carries the HTTP status the caller should respond with.
 */
public class QueueApprovalException extends RuntimeException {

    private final Response.Status status;

    public QueueApprovalException(Response.Status status, String message) {
        super(message);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }
}
