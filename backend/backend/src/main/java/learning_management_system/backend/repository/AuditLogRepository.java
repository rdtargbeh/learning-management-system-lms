package learning_management_system.backend.repository;

import learning_management_system.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/** Audit log; searched with AuditLogSpecification. RLS limits rows to the caller's tenant (platform admins see every tenant). */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {
}
