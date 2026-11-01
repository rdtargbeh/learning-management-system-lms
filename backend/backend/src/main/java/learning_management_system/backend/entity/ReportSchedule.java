package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.DistributionFormat;
import learning_management_system.backend.enums.ScheduleFrequency;
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
 * Table <b>report_schedule</b> (Learning Analytics & Reporting domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "report_schedule", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ReportSchedule implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "schedule_id", updatable = false)
    private UUID scheduleId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    @Column(name = "scheduled_by", nullable = false)
    private UUID scheduledBy;

    @Column(name = "schedule_name", length = 255)
    private String scheduleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_frequency", nullable = false, length = 50)
    private ScheduleFrequency scheduleFrequency;

    @Column(name = "schedule_time")
    private LocalTime scheduleTime;

    @Column(name = "schedule_day_of_week")
    private Integer scheduleDayOfWeek;

    @Column(name = "schedule_day_of_month")
    private Integer scheduleDayOfMonth;

    @Column(name = "schedule_timezone", length = 50)
    private String scheduleTimezone;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "next_run_at")
    private OffsetDateTime nextRunAt;

    @Column(name = "last_run_at")
    private OffsetDateTime lastRunAt;

    @Column(name = "last_run_status", length = 50)
    private String lastRunStatus;

    @Column(name = "run_count")
    private Integer runCount;

    @Column(name = "failure_count")
    private Integer failureCount;

    @Column(name = "distribution_enabled")
    private Boolean distributionEnabled;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "distribution_recipients")
    private JsonNode distributionRecipients;

    @Enumerated(EnumType.STRING)
    @Column(name = "distribution_format", length = 50)
    private DistributionFormat distributionFormat;

    @Column(name = "notification_on_failure")
    private Boolean notificationOnFailure;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notification_recipients")
    private JsonNode notificationRecipients;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "max_runs")
    private Integer maxRuns;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
