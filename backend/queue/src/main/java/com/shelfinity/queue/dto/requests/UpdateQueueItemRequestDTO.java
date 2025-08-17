package com.shelfinity.queue.dto.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.shelfinity.queue.dto.enums.QueueStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateQueueItemRequest")
public class UpdateQueueItemRequestDTO {
    @NotNull
    @Schema(description = "New status")
    public QueueStatus status;

    @Size(max = 500)
    @Schema(description = "Optional remark")
    public String remark;
}
