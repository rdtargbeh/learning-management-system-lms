package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.StudentLearningSessionStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>student_learning_session</b> (Content Delivery & Learning Activity domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "student_learning_session", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class StudentLearningSession implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "stud_session_id", updatable = false)
    private UUID studSessionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "session_start", nullable = false)
    private OffsetDateTime sessionStart;

    @Column(name = "session_end")
    private OffsetDateTime sessionEnd;

    @Column(name = "session_duration_minutes")
    private Integer sessionDurationMinutes;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", length = 50)
    private StudentLearningSessionStatus sessionStatus;

    @Column(name = "total_events")
    private Integer totalEvents;

    @Column(name = "pages_visited")
    private Integer pagesVisited;

    @Column(name = "content_accessed")
    private Integer contentAccessed;

    @Column(name = "videos_watched")
    private Integer videosWatched;
}
