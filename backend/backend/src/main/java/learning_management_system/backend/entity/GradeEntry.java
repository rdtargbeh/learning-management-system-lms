package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>grade_entry</b> (Grading & Evaluation domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "grade_entry", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class GradeEntry implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "entry_id", updatable = false)
    private UUID entryId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "gradebook_id", nullable = false)
    private UUID gradebookId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "score", precision = 8, scale = 2)
    private BigDecimal score;

    @Column(name = "override_score", precision = 8, scale = 2)
    private BigDecimal overrideScore;

    @Column(name = "late_penalty_applied", precision = 8, scale = 2)
    private BigDecimal latePenaltyApplied;

    @Column(name = "points_earned", precision = 8, scale = 2)
    private BigDecimal pointsEarned;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "letter_grade", length = 5)
    private String letterGrade;

    @Column(name = "grade_date")
    private OffsetDateTime gradeDate;

    @Column(name = "graded_by")
    private UUID gradedBy;

    @Column(name = "grading_notes")
    private String gradingNotes;

    @Column(name = "is_excluded")
    private Boolean isExcluded;

    @Column(name = "is_incomplete")
    private Boolean isIncomplete;

    @Column(name = "is_posted", nullable = false)
    private Boolean isPosted;

    @Column(name = "date_posted")
    private OffsetDateTime datePosted;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
