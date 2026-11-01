package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.UsageType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>external_tool_usage</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "external_tool_usage", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ExternalToolUsage implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "usage_id", updatable = false)
    private UUID usageId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "tool_id", nullable = false)
    private UUID toolId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "content_id")
    private UUID contentId;

    @Column(name = "assignment_id")
    private UUID assignmentId;

    @Column(name = "assessment_id")
    private UUID assessmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false, length = 50)
    private UsageType usageType;

    @Column(name = "usage_context", length = 50)
    private String usageContext;

    @Column(name = "launch_count")
    private Integer launchCount;

    @Column(name = "total_time_minutes")
    private Integer totalTimeMinutes;

    @Column(name = "first_used", nullable = false)
    private OffsetDateTime firstUsed;

    @Column(name = "last_used", nullable = false)
    private OffsetDateTime lastUsed;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "usage_success_rate", precision = 5, scale = 2)
    private BigDecimal usageSuccessRate;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "user_feedback_rating", precision = 3, scale = 1)
    private BigDecimal userFeedbackRating;

    @Column(name = "user_feedback_text")
    private String userFeedbackText;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
