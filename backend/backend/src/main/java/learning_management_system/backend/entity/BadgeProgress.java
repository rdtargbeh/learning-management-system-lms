package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>badge_progress</b> (Gamification & Achievements domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "badge_progress", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class BadgeProgress implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "progress_id", updatable = false)
    private UUID progressId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "badge_id", nullable = false)
    private UUID badgeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "progress_percentage", precision = 5, scale = 2)
    private BigDecimal progressPercentage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "criteria_met")
    private JsonNode criteriaMet;

    @Column(name = "current_value")
    private Integer currentValue;

    @Column(name = "target_value")
    private Integer targetValue;

    @Column(name = "last_updated", nullable = false)
    private OffsetDateTime lastUpdated;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @Column(name = "completion_date")
    private OffsetDateTime completionDate;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
