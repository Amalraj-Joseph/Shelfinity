/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues.dto.requests;

import com.shelfinity.queues.QueueStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating a queue item (admin approval/rejection).
 */
public class UpdateQueueItemRequest {
    
    @NotNull(message = "Status is required")
    private QueueStatus status;
    
    private String adminRemark;
    
    // Default constructor
    public UpdateQueueItemRequest() {}
    
    // Constructor with required fields
    public UpdateQueueItemRequest(QueueStatus status) {
        this.status = status;
    }
    
    // Constructor with all fields
    public UpdateQueueItemRequest(QueueStatus status, String adminRemark) {
        this.status = status;
        this.adminRemark = adminRemark;
    }
    
    // Getters and Setters
    public QueueStatus getStatus() {
        return status;
    }
    
    public void setStatus(QueueStatus status) {
        this.status = status;
    }
    
    public String getAdminRemark() {
        return adminRemark;
    }
    
    public void setAdminRemark(String adminRemark) {
        this.adminRemark = adminRemark;
    }
    
    @Override
    public String toString() {
        return "UpdateQueueItemRequest{" +
                "status=" + status +
                ", adminRemark='" + adminRemark + '\'' +
                '}';
    }
}
