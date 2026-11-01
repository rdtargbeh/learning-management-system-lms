package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.PolicyCategory;
import learning_management_system.backend.enums.PolicyStatus;
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
 * Table <b>institutional_policy</b> (Knowledge Base & Institutional Content domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "institutional_policy", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class InstitutionalPolicy implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "policy_id", updatable = false)
    private UUID policyId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "policy_title", nullable = false, length = 255)
    private String policyTitle;

    @Column(name = "policy_code", length = 100)
    private String policyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_category", nullable = false, length = 100)
    private PolicyCategory policyCategory;

    @Column(name = "policy_description")
    private String policyDescription;

    @Column(name = "policy_content", nullable = false)
    private String policyContent;

    @Column(name = "policy_scope", length = 100)
    private String policyScope;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "approval_date")
    private OffsetDateTime approvalDate;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_status", nullable = false, length = 50)
    private PolicyStatus policyStatus;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Column(name = "requires_acknowledgment")
    private Boolean requiresAcknowledgment;

    @Column(name = "acknowledgment_deadline")
    private LocalDate acknowledgmentDeadline;

    @Column(name = "owner_department", length = 255)
    private String ownerDepartment;

    @Column(name = "responsible_user_id")
    private UUID responsibleUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "revision_history")
    private JsonNode revisionHistory;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "related_policies")
    private JsonNode relatedPolicies;

    @Column(name = "policy_url", length = 500)
    private String policyUrl;

    @Column(name = "compliance_requirement", length = 100)
    private String complianceRequirement;

    @Column(name = "enforcement_method")
    private String enforcementMethod;

    @Column(name = "penalty_for_violation")
    private String penaltyForViolation;

    @Column(name = "seo_keywords", length = 255)
    private String seoKeywords;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
