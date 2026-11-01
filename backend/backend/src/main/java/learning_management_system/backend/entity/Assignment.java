package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AssignmentGradingType;
import learning_management_system.backend.enums.AssignmentStatus;
import learning_management_system.backend.enums.AssignmentType;
import learning_management_system.backend.enums.SubmissionType;
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
 * Table <b>assignment</b> (Assignments & Coursework domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "assignment", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Assignment implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "assignment_id", updatable = false)
    private UUID assignmentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "assignment_title", nullable = false, length = 255)
    private String assignmentTitle;

    @Column(name = "assignment_description")
    private String assignmentDescription;

    @Column(name = "assignment_instructions")
    private String assignmentInstructions;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 50)
    private AssignmentType assignmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status", nullable = false, length = 50)
    private AssignmentStatus assignmentStatus;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "points_possible", precision = 8, scale = 2)
    private BigDecimal pointsPossible;

    @Column(name = "omit_from_final_grade", nullable = false)
    private Boolean omitFromFinalGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "grading_type", length = 50)
    private AssignmentGradingType gradingType;

    @Column(name = "rubric_template_id")
    private UUID rubricTemplateId;

    @Column(name = "is_anonymous_grading", nullable = false)
    private Boolean isAnonymousGrading;

    @Column(name = "is_moderated_grading", nullable = false)
    private Boolean isModeratedGrading;

    @Column(name = "final_grader_id")
    private UUID finalGraderId;

    @Column(name = "grader_count")
    private Integer graderCount;

    @Column(name = "graders_anonymous_to_graders", nullable = false)
    private Boolean gradersAnonymousToGraders;

    @Column(name = "allow_submissions")
    private Boolean allowSubmissions;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_type", nullable = false, length = 50)
    private SubmissionType submissionType;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_submission_types")
    private String[] allowedSubmissionTypes;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_file_extensions")
    private String[] allowedFileExtensions;

    @Column(name = "annotatable_file_id")
    private UUID annotatableFileId;

    @Column(name = "external_tool_id")
    private UUID externalToolId;

    @Column(name = "max_submissions")
    private Integer maxSubmissions;

    @Column(name = "late_submission_allowed")
    private Boolean lateSubmissionAllowed;

    @Column(name = "late_submission_penalty_percent", precision = 5, scale = 2)
    private BigDecimal lateSubmissionPenaltyPercent;

    @Column(name = "date_available")
    private LocalDate dateAvailable;

    @Column(name = "date_due")
    private LocalDate dateDue;

    @Column(name = "date_close")
    private LocalDate dateClose;

    @Column(name = "allow_group_submission")
    private Boolean allowGroupSubmission;

    @Column(name = "group_set_id")
    private UUID groupSetId;

    @Column(name = "grade_group_individually", nullable = false)
    private Boolean gradeGroupIndividually;

    @Column(name = "group_size")
    private Integer groupSize;

    @Column(name = "peer_review_required")
    private Boolean peerReviewRequired;

    @Column(name = "peer_review_count")
    private Integer peerReviewCount;

    @Column(name = "plagiarism_detection_enabled")
    private Boolean plagiarismDetectionEnabled;

    @Column(name = "plagiarism_detection_tool", length = 50)
    private String plagiarismDetectionTool;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
