package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AccessibilityStandardType;
import learning_management_system.backend.enums.AccessibilityStandardWcagLevel;
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
 * Table <b>accessibility_standard</b> (Learning Standards & Compliance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "accessibility_standard", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AccessibilityStandard implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "standard_id", updatable = false)
    private UUID standardId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "standard_name", nullable = false, length = 255)
    private String standardName;

    @Column(name = "standard_code", length = 50)
    private String standardCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "wcag_level", length = 10)
    private AccessibilityStandardWcagLevel wcagLevel;

    @Column(name = "wcag_criteria", length = 50)
    private String wcagCriteria;

    @Column(name = "standard_description")
    private String standardDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "standard_type", nullable = false, length = 50)
    private AccessibilityStandardType standardType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_to")
    private JsonNode applicableTo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "compliance_checklist")
    private JsonNode complianceChecklist;

    @Column(name = "testing_procedure")
    private String testingProcedure;

    @Column(name = "remediation_guidance")
    private String remediationGuidance;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tools_required")
    private JsonNode toolsRequired;

    @Column(name = "is_critical")
    private Boolean isCritical;

    @Column(name = "is_auditable")
    private Boolean isAuditable;

    @Column(name = "current_compliance_percentage", precision = 5, scale = 2)
    private BigDecimal currentCompliancePercentage;

    @Column(name = "last_audit_date")
    private LocalDate lastAuditDate;

    @Column(name = "next_audit_date")
    private LocalDate nextAuditDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "audit_findings")
    private JsonNode auditFindings;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
