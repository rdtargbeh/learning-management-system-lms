package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.SystemResourceLibraryResourceType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>system_resource_library</b> (Learning Content Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "system_resource_library", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class SystemResourceLibrary implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "resource_id", updatable = false)
    private UUID resourceId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "resource_name", nullable = false, length = 255)
    private String resourceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 50)
    private SystemResourceLibraryResourceType resourceType;

    @Column(name = "resource_url", length = 500)
    private String resourceUrl;

    @Column(name = "resource_description")
    private String resourceDescription;

    @Column(name = "creator_user_id")
    private UUID creatorUserId;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "date_approved")
    private OffsetDateTime dateApproved;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "usage_count")
    private Integer usageCount;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "download_count")
    private Integer downloadCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags")
    private JsonNode tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private JsonNode metadata;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
