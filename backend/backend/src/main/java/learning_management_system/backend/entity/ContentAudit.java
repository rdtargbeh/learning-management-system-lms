package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AuditStatus;
import learning_management_system.backend.enums.AuditType;
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
 * Table <b>content_audit</b> (Knowledge Base & Institutional Content domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "content_audit", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ContentAudit implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id", updatable = false)
    private UUID auditId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "object_id", nullable = false)
    private UUID objectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_type", nullable = false, length = 50)
    private AuditType auditType;

    @Column(name = "auditor_id")
    private UUID auditorId;

    @Column(name = "audit_date", nullable = false)
    private OffsetDateTime auditDate;

    @Column(name = "audit_findings")
    private String auditFindings;

    @Column(name = "accessibility_score", precision = 5, scale = 2)
    private BigDecimal accessibilityScore;

    @Column(name = "quality_score", precision = 5, scale = 2)
    private BigDecimal qualityScore;

    @Column(name = "relevance_score", precision = 5, scale = 2)
    private BigDecimal relevanceScore;

    @Column(name = "accuracy_score", precision = 5, scale = 2)
    private BigDecimal accuracyScore;

    @Column(name = "completeness_score", precision = 5, scale = 2)
    private BigDecimal completenessScore;

    @Column(name = "overall_score", precision = 5, scale = 2)
    private BigDecimal overallScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "issues_identified")
    private JsonNode issuesIdentified;

    @Column(name = "recommendations")
    private String recommendations;

    @Column(name = "remediation_required")
    private Boolean remediationRequired;

    @Column(name = "remediation_deadline")
    private LocalDate remediationDeadline;

    @Column(name = "remediation_status", length = 50)
    private String remediationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_status", nullable = false, length = 50)
    private AuditStatus auditStatus;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
