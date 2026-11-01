package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.StorageQuotaContextType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>storage_quota</b> (File Management & Storage domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "storage_quota", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class StorageQuota implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "quota_id", updatable = false)
    private UUID quotaId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "context_type", nullable = false, length = 20)
    private StorageQuotaContextType contextType;

    @Column(name = "context_id", nullable = false)
    private UUID contextId;

    @Column(name = "quota_bytes", nullable = false)
    private Long quotaBytes;

    @Column(name = "used_bytes", nullable = false)
    private Long usedBytes;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
