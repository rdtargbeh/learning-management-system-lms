package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AttemptStatus;
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
 * Table <b>assessment_attempt</b> (Assessment & Testing domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "assessment_attempt", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AssessmentAttempt implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "attempt_id", updatable = false)
    private UUID attemptId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "date_started", nullable = false)
    private OffsetDateTime dateStarted;

    @Column(name = "date_submitted")
    private OffsetDateTime dateSubmitted;

    @Column(name = "date_completed")
    private OffsetDateTime dateCompleted;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_status", nullable = false, length = 50)
    private AttemptStatus attemptStatus;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "flagged_for_review")
    private Boolean flaggedForReview;

    @Column(name = "suspicious_activity_detected")
    private Boolean suspiciousActivityDetected;

    @Column(name = "review_notes")
    private String reviewNotes;

    @Column(name = "date_reviewed")
    private OffsetDateTime dateReviewed;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
