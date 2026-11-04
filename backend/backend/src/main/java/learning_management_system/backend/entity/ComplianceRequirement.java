package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ComplianceRequirementType;
import learning_management_system.backend.enums.ComplianceStatus;
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
 * Table <b>compliance_requirement</b> (Learning Standards & Compliance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "compliance_requirement", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ComplianceRequirement implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "requirement_id", updatable = false)
    private UUID requirementId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "requirement_name", nullable = false, length = 255)
    private String requirementName;

    @Column(name = "requirement_description")
    private String requirementDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type", nullable = false, length = 50)
    private ComplianceRequirementType requirementType;

    @Column(name = "jurisdiction", length = 100)
    private String jurisdiction;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_regulations")
    private JsonNode applicableRegulations;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_status", nullable = false, length = 50)
    private ComplianceStatus complianceStatus;

    @Column(name = "responsible_department", length = 255)
    private String responsibleDepartment;

    @Column(name = "responsible_user_id")
    private UUID responsibleUserId;

    @Column(name = "evidence_required")
    private String evidenceRequired;

    @Column(name = "evidence_submission_deadline")
    private LocalDate evidenceSubmissionDeadline;

    @Column(name = "audit_required")
    private Boolean auditRequired;

    @Column(name = "audit_frequency", length = 50)
    private String auditFrequency;

    @Column(name = "penalty_for_non_compliance")
    private String penaltyForNonCompliance;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resource_links")
    private JsonNode resourceLinks;

    @Column(name = "implementation_notes")
    private String implementationNotes;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
