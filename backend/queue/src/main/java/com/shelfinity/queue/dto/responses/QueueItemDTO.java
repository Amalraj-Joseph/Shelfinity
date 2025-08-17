package com.shelfinity.queue.dto.responses;

import java.time.Instant;
import java.util.UUID;

public class QueueItemDTO {
    public UUID id;
    public UUID itemId;
    public String itemType;
    public String status;
    public String remark;
    public Instant createdAt;
    public Instant updatedAt;
}