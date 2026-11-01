package learning_management_system.backend.entity;

import jakarta.persistence.*;
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
 * Table <b>student_badge</b> (Gamification & Achievements domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "student_badge", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class StudentBadge implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "student_badge_id", updatable = false)
    private UUID studentBadgeId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "badge_id", nullable = false)
    private UUID badgeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "earned_date", nullable = false)
    private OffsetDateTime earnedDate;

    @Column(name = "earned_in_course_id")
    private UUID earnedInCourseId;

    @Column(name = "earned_in_section_id")
    private UUID earnedInSectionId;

    @Column(name = "earned_through_activity_id")
    private UUID earnedThroughActivityId;

    @Column(name = "earned_through_activity_type", length = 100)
    private String earnedThroughActivityType;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @Column(name = "pinned_date")
    private OffsetDateTime pinnedDate;

    @Column(name = "is_visible")
    private Boolean isVisible;

    @Column(name = "assertion_url", length = 1000)
    private String assertionUrl;

    @Column(name = "date_expires")
    private OffsetDateTime dateExpires;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
