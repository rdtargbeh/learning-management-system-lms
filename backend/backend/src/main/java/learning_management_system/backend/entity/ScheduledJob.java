package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.JobType;
import learning_management_system.backend.enums.ScheduleType;
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
 * Table <b>scheduled_job</b> (System Administration & Maintenance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "scheduled_job", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ScheduledJob implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "job_id", updatable = false)
    private UUID jobId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "job_name", nullable = false, length = 255)
    private String jobName;

    @Column(name = "job_description")
    private String jobDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 50)
    private JobType jobType;

    @Column(name = "job_handler", nullable = false, length = 500)
    private String jobHandler;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "job_parameters")
    private JsonNode jobParameters;

    @Column(name = "schedule_expression", length = 100)
    private String scheduleExpression;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 50)
    private ScheduleType scheduleType;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "run_immediately")
    private Boolean runImmediately;

    @Column(name = "max_concurrent_instances")
    private Integer maxConcurrentInstances;

    @Column(name = "current_running_count")
    private Integer currentRunningCount;

    @Column(name = "retry_on_failure")
    private Boolean retryOnFailure;

    @Column(name = "max_retry_attempts")
    private Integer maxRetryAttempts;

    @Column(name = "retry_interval_seconds")
    private Integer retryIntervalSeconds;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "run_on_all_tenants")
    private Boolean runOnAllTenants;

    @Column(name = "execution_history_retention_days")
    private Integer executionHistoryRetentionDays;

    @Column(name = "is_system_job")
    private Boolean isSystemJob;

    @Column(name = "allow_manual_trigger")
    private Boolean allowManualTrigger;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "last_modified_by")
    private UUID lastModifiedBy;

    @Column(name = "last_execution_id")
    private UUID lastExecutionId;

    @Column(name = "next_scheduled_run")
    private OffsetDateTime nextScheduledRun;

    @Column(name = "disabled_reason")
    private String disabledReason;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
