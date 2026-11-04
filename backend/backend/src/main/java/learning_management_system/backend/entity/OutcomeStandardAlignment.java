package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.OutcomeStandardAlignmentStrength;
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
 * Table <b>outcome_standard_alignment</b> (Learning Standards & Compliance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "outcome_standard_alignment", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class OutcomeStandardAlignment implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "alignment_id", updatable = false)
    private UUID alignmentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_outcome_id")
    private UUID courseOutcomeId;

    @Column(name = "assessment_id")
    private UUID assessmentId;

    @Column(name = "standard_id", nullable = false)
    private UUID standardId;

    @Column(name = "framework_id")
    private UUID frameworkId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alignment_strength", nullable = false, length = 50)
    private OutcomeStandardAlignmentStrength alignmentStrength;

    @Column(name = "achievement_threshold", precision = 5, scale = 2)
    private BigDecimal achievementThreshold;

    @Column(name = "mastery_indicator")
    private String masteryIndicator;

    @Column(name = "assessment_evidence")
    private String assessmentEvidence;

    @Column(name = "alignment_notes")
    private String alignmentNotes;

    @Column(name = "is_primary_outcome")
    private Boolean isPrimaryOutcome;

    @Column(name = "alignment_verified")
    private Boolean alignmentVerified;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verification_date")
    private OffsetDateTime verificationDate;

    @Column(name = "expected_proficiency_level", length = 50)
    private String expectedProficiencyLevel;

    @Column(name = "actual_proficiency_level", length = 50)
    private String actualProficiencyLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "proficiency_data")
    private JsonNode proficiencyData;

    @Column(name = "alignment_status", length = 50)
    private String alignmentStatus;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
