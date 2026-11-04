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
 * Table <b>ebook_usage_analytics</b> (E-Book Platform domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "ebook_usage_analytics", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class EbookUsageAnalytics implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "analytics_id", updatable = false)
    private UUID analyticsId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "ebook_id", nullable = false)
    private UUID ebookId;

    @Column(name = "total_students_assigned")
    private Integer totalStudentsAssigned;

    @Column(name = "students_accessed")
    private Integer studentsAccessed;

    @Column(name = "students_completed")
    private Integer studentsCompleted;

    @Column(name = "avg_reading_time_minutes", precision = 8, scale = 2)
    private BigDecimal avgReadingTimeMinutes;

    @Column(name = "avg_completion_percent", precision = 5, scale = 2)
    private BigDecimal avgCompletionPercent;

    @Column(name = "most_read_chapter_id")
    private UUID mostReadChapterId;

    @Column(name = "least_read_chapter_id")
    private UUID leastReadChapterId;

    @Column(name = "avg_assessment_score", precision = 5, scale = 2)
    private BigDecimal avgAssessmentScore;

    @Column(name = "last_updated", nullable = false)
    private OffsetDateTime lastUpdated;
}
