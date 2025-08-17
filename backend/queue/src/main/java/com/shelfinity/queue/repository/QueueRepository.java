package com.shelfinity.queue.repository;

import com.shelfinity.queue.entity.QueueItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class QueueRepository {
    @PersistenceContext(unitName = "default")
    EntityManager em;

    public QueueItem get(UUID id){ return em.find(QueueItem.class, id); }
    public void delete(QueueItem item){ em.remove(item); }

    public List<QueueItem> list(String type, String status, int offset, int limit){
        StringBuilder jpql = new StringBuilder("SELECT q FROM QueueItem q WHERE 1=1 ");
        if (type != null) jpql.append("AND q.itemType = :type ");
        if (status != null) jpql.append("AND q.status = :status ");
        jpql.append("ORDER BY q.createdAt ASC");
        var q = em.createQuery(jpql.toString(), QueueItem.class);
        if (type != null) q.setParameter("type", Enum.valueOf(QueueItem.ItemType.class, type));
        if (status != null) q.setParameter("status", Enum.valueOf(QueueItem.Status.class, status));
        return q.setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    public void updateQueueRow(QueueItem item){
        em.merge(item);
    }

    // ---- Cross-table updates (decoupled via native SQL) ----

    public int patchUserRegistration(UUID itemId, String status, String remark){
        StringBuilder sb = new StringBuilder("UPDATE user_reg_requests SET ");
        boolean first = true;
        if (status != null){ sb.append("status = :status"); first=false; }
        if (remark != null){ sb.append(first? "" : ", ").append("remark = :remark"); }
        sb.append(" WHERE id = :id");
        var q = em.createNativeQuery(sb.toString());
        if (status != null) q.setParameter("status", status);
        if (remark != null) q.setParameter("remark", remark);
        q.setParameter("id", itemId);
        return q.executeUpdate();
    }

    public int patchReservation(UUID itemId, String status, String remark){
        // Try update with remark; if column doesn't exist, retry with status-only
        int updated = 0;
        if (status == null && remark == null) return 0;
        try {
            StringBuilder sb = new StringBuilder("UPDATE reservations SET ");
            boolean first = true;
            if (status != null){ sb.append("status = :status"); first=false; }
            if (remark != null){ sb.append(first? "" : ", ").append("remark = :remark"); }
            sb.append(" WHERE id = :id");
            var q = em.createNativeQuery(sb.toString());
            if (status != null) q.setParameter("status", status);
            if (remark != null) q.setParameter("remark", remark);
            q.setParameter("id", itemId);
            updated = q.executeUpdate();
        } catch (PersistenceException e){
            if (status != null){
                var q2 = em.createNativeQuery("UPDATE reservations SET status = :status WHERE id = :id");
                q2.setParameter("status", status); q2.setParameter("id", itemId);
                updated = q2.executeUpdate();
            }
        }
        return updated;
    }
}