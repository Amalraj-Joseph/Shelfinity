package com.shelfinity.queue.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.shelfinity.queue.dto.requests.UpdateQueueItemRequestDTO;
import com.shelfinity.queue.dto.responses.QueueItemDTO;
import com.shelfinity.queue.entity.QueueItem;
import com.shelfinity.queue.repository.QueueRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class QueueService {
    @Inject QueueRepository repo;

    // Keep existing signature (repo expects String for status filter)
    public List<QueueItemDTO> list(String type, String status, int offset, int limit){
        return repo.list(type, status, offset, limit)
                   .stream()
                   .map(this::toDTO)
                   .collect(Collectors.toList());
    }

    // Optional overload if your API layer now passes an enum:
    public List<QueueItemDTO> list(String type,
                                   com.shelfinity.queue.dto.enums.QueueStatus status,
                                   int offset, int limit) {
        return list(type, status != null ? status.name() : null, offset, limit);
    }

    public QueueItemDTO get(UUID id){
        QueueItem q = repo.get(id);
        return q == null ? null : toDTO(q);
    }

    @Transactional
    public void delete(UUID id){
        QueueItem q = repo.get(id);
        if (q != null) repo.delete(q);
    }

    @Transactional
    public void patch(UUID id, UpdateQueueItemRequestDTO in){
        QueueItem q = repo.get(id);
        if (q == null) throw new IllegalArgumentException("Queue item not found");

        boolean changed = false;

        if (in.status != null) {
            // Map DTO enum -> entity enum
            q.setStatus(QueueItem.Status.valueOf(in.status.name()));
            changed = true;
        }
        if (in.remark != null) {
            q.setRemark(in.remark);
            changed = true;
        }

        if (changed) {
            repo.updateQueueRow(q);
        }

        // Propagate to underlying table using decoupled native SQL
        final String statusStr = (in.status != null) ? in.status.name() : null;
        switch (q.getItemType()) {
            case USER_REG_REQUEST -> repo.patchUserRegistration(q.getItemId(), statusStr, in.remark);
            case RESERVATION      -> repo.patchReservation(q.getItemId(), statusStr, in.remark);
        }
    }

    private QueueItemDTO toDTO(QueueItem q){
        QueueItemDTO d = new QueueItemDTO();
        d.id = q.getId();
        d.itemId = q.getItemId();
        d.itemType = q.getItemType().name();
        d.status = q.getStatus().name();
        d.remark = q.getRemark();
        d.createdAt = q.getCreatedAt();
        d.updatedAt = q.getUpdatedAt();
        return d;
    }
}
