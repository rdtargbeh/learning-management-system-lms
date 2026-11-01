package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.EbookChapterAssessmentType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>ebook_chapter_assessment</b> (E-Book Platform domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "ebook_chapter_assessment", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class EbookChapterAssessment implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ebook_assessment_id", updatable = false)
    private UUID ebookAssessmentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "chapter_id", nullable = false)
    private UUID chapterId;

    @Column(name = "assessment_title", nullable = false, length = 255)
    private String assessmentTitle;

    @Column(name = "assessment_description")
    private String assessmentDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", length = 50)
    private EbookChapterAssessmentType assessmentType;

    @Column(name = "min_score_to_pass", precision = 5, scale = 2)
    private BigDecimal minScoreToPass;

    @Column(name = "allow_multiple_attempts")
    private Boolean allowMultipleAttempts;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "show_correct_answers")
    private Boolean showCorrectAnswers;

    @Column(name = "show_score_immediately")
    private Boolean showScoreImmediately;

    @Column(name = "is_required_to_progress")
    private Boolean isRequiredToProgress;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
