package learning_management_system.backend.specification;

import jakarta.persistence.criteria.Predicate;
import learning_management_system.backend.entity.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Audit-log search filters; only the filters that are given become part of the query. */
public final class AuditLogSpecification {

    private AuditLogSpecification() {
    }

    public static Specification<AuditLog> matching(UUID tenantId, UUID actorUserId, UUID affectedUserId, String actionType,
                                                   String resourceType, UUID resourceId, OffsetDateTime from, OffsetDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (tenantId != null) p.add(cb.equal(root.get("tenantId"), tenantId));
            if (actorUserId != null) p.add(cb.equal(root.get("actorUserId"), actorUserId));
            if (affectedUserId != null) p.add(cb.equal(root.get("affectedUserId"), affectedUserId));
            if (actionType != null) p.add(cb.equal(root.get("actionType"), actionType));
            if (resourceType != null) p.add(cb.equal(root.get("resourceType"), resourceType));
            if (resourceId != null) p.add(cb.equal(root.get("resourceId"), resourceId));
            if (from != null) p.add(cb.greaterThanOrEqualTo(root.get("dateCreated"), from));
            if (to != null) p.add(cb.lessThan(root.get("dateCreated"), to));
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}
