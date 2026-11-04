package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Table <b>compliance_audit</b> (Audit & Logging domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "compliance_audit", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ComplianceAudit implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id", updatable = false)
    private UUID auditId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "compliance_type", length = 50)
    private String complianceType;

    @Column(name = "action_taken", length = 255)
    private String actionTaken;

    @Column(name = "data_accessed")
    private String dataAccessed;

    @Column(name = "justification")
    private String justification;

    @Column(name = "is_compliant")
    private Boolean isCompliant;

    @Column(name = "audit_notes")
    private String auditNotes;

    @Column(name = "date_audit", nullable = false)
    private OffsetDateTime dateAudit;
}
