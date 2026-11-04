package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AssignmentSubmissionStatus;
import learning_management_system.backend.enums.LatePolicyStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>assignment_submission</b> (Assignments & Coursework domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "assignment_submission", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AssignmentSubmission implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "submission_id", updatable = false)
    private UUID submissionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "submission_number", nullable = false)
    private Integer submissionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_status", nullable = false, length = 50)
    private AssignmentSubmissionStatus submissionStatus;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "submitted_late")
    private Boolean submittedLate;

    @Enumerated(EnumType.STRING)
    @Column(name = "late_policy_status", nullable = false, length = 20)
    private LatePolicyStatus latePolicyStatus;

    @Column(name = "seconds_late_override")
    private Long secondsLateOverride;

    @Column(name = "is_excused", nullable = false)
    private Boolean isExcused;

    @Column(name = "submission_type_used", length = 30)
    private String submissionTypeUsed;

    @Column(name = "is_group_submission")
    private Boolean isGroupSubmission;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "submission_text")
    private String submissionText;

    @Column(name = "submission_url", length = 500)
    private String submissionUrl;

    @Column(name = "submission_notes")
    private String submissionNotes;

    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "character_count")
    private Integer characterCount;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
