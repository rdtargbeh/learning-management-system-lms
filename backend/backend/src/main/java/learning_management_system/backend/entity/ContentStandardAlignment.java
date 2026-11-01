package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AlignmentStatus;
import learning_management_system.backend.enums.ContentStandardAlignmentStrength;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>content_standard_alignment</b> (Learning Standards & Compliance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "content_standard_alignment", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ContentStandardAlignment implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "alignment_id", updatable = false)
    private UUID alignmentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column(name = "standard_id", nullable = false)
    private UUID standardId;

    @Column(name = "framework_id")
    private UUID frameworkId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alignment_strength", nullable = false, length = 50)
    private ContentStandardAlignmentStrength alignmentStrength;

    @Column(name = "alignment_percentage", precision = 5, scale = 2)
    private BigDecimal alignmentPercentage;

    @Column(name = "alignment_notes")
    private String alignmentNotes;

    @Column(name = "assessment_method", length = 100)
    private String assessmentMethod;

    @Column(name = "assessment_tool_id")
    private UUID assessmentToolId;

    @Column(name = "is_primary_alignment")
    private Boolean isPrimaryAlignment;

    @Column(name = "alignment_verified")
    private Boolean alignmentVerified;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verification_date")
    private OffsetDateTime verificationDate;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "alignment_status", length = 50)
    private AlignmentStatus alignmentStatus;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
