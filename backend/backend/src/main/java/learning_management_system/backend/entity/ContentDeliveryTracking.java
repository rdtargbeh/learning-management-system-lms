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
 * Table <b>content_delivery_tracking</b> (Content Delivery & Learning Activity domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "content_delivery_tracking", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ContentDeliveryTracking implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tracking_id", updatable = false)
    private UUID trackingId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column(name = "total_students_assigned")
    private Integer totalStudentsAssigned;

    @Column(name = "total_students_accessed")
    private Integer totalStudentsAccessed;

    @Column(name = "total_views")
    private Integer totalViews;

    @Column(name = "avg_completion_percent", precision = 5, scale = 2)
    private BigDecimal avgCompletionPercent;

    @Column(name = "avg_time_spent_minutes", precision = 8, scale = 2)
    private BigDecimal avgTimeSpentMinutes;

    @Column(name = "first_accessed")
    private OffsetDateTime firstAccessed;

    @Column(name = "last_accessed")
    private OffsetDateTime lastAccessed;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
