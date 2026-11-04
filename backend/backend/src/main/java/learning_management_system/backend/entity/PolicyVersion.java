package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ChangeType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>policy_version</b> (Knowledge Base & Institutional Content domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "policy_version", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class PolicyVersion implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "version_id", updatable = false)
    private UUID versionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "version_title", length = 255)
    private String versionTitle;

    @Column(name = "version_content", nullable = false)
    private String versionContent;

    @Column(name = "previous_version_id")
    private UUID previousVersionId;

    @Column(name = "change_summary")
    private String changeSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", length = 50)
    private ChangeType changeType;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

    @Column(name = "is_current")
    private Boolean isCurrent;

    @Column(name = "approval_date")
    private OffsetDateTime approvalDate;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "version_metadata")
    private JsonNode versionMetadata;
}
