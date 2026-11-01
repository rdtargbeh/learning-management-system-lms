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

/**
 * Table <b>user_chapter_assessment_attempt</b> (E-Book Platform domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "user_chapter_assessment_attempt", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class UserChapterAssessmentAttempt implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "attempt_id", updatable = false)
    private UUID attemptId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "ebook_assessment_id", nullable = false)
    private UUID ebookAssessmentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "passed")
    private Boolean passed;

    @Column(name = "date_started", nullable = false)
    private OffsetDateTime dateStarted;

    @Column(name = "date_completed")
    private OffsetDateTime dateCompleted;

    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes;
}
