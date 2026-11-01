package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.FinalGradeSubmissionStatus;
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
 * Table <b>final_grade_submission</b> (Grading & Evaluation domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "final_grade_submission", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class FinalGradeSubmission implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "grade_submission_id", updatable = false)
    private UUID gradeSubmissionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "sis_integration_id")
    private UUID sisIntegrationId;

    @Column(name = "submitted_by")
    private UUID submittedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_status", nullable = false, length = 20)
    private FinalGradeSubmissionStatus submissionStatus;

    @Column(name = "grade_count", nullable = false)
    private Integer gradeCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload")
    private JsonNode payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response")
    private JsonNode response;

    @Column(name = "date_submitted")
    private OffsetDateTime dateSubmitted;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
