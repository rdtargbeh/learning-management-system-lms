package learning_management_system.backend.utility;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * Re-reads a row after INSERT so columns filled by the database (defaults such as course_status = 'UNPUBLISHED',
 * counters, flags, trigger values) are visible in the response. Call inside the current transaction.
 */
@Component
public class EntityRefresher {
    private final EntityManager em;

    public EntityRefresher(EntityManager em) {
        this.em = em;
    }

    public <T> T refresh(T entity) {
        em.flush();
        em.refresh(entity);
        return entity;
    }
}
