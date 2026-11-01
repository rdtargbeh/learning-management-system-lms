package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.TenantStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>tenant</b> (Tenancy & Institution Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "tenant", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tenant_id", updatable = false)
    private UUID tenantId;

    @Column(name = "tenant_name", nullable = false, length = 255)
    private String tenantName;

    @Column(name = "tenant_code", length = 50)
    private String tenantCode;

    @Column(name = "tenant_description")
    private String tenantDescription;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_status", length = 20)
    private TenantStatus tenantStatus;

    @Column(name = "data_region", length = 50)
    private String dataRegion;

    @Column(name = "default_timezone", nullable = false, length = 50)
    private String defaultTimezone;

    @Column(name = "default_locale", nullable = false, length = 10)
    private String defaultLocale;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private JsonNode metadata;

    @Column(name = "created_by")
    private UUID createdBy;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Column(name = "suspended_at")
    private OffsetDateTime suspendedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
