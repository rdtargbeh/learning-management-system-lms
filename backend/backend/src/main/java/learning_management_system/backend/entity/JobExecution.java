package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ExecutionStatus;
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
 * Table <b>job_execution</b> (System Administration & Maintenance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "job_execution", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class JobExecution implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "execution_id", updatable = false)
    private UUID executionId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false, length = 50)
    private ExecutionStatus executionStatus;

    @Column(name = "execution_start_time", nullable = false)
    private OffsetDateTime executionStartTime;

    @Column(name = "execution_end_time")
    private OffsetDateTime executionEndTime;

    @Column(name = "execution_duration_seconds")
    private Integer executionDurationSeconds;

    @Column(name = "total_runtime_seconds")
    private Integer totalRuntimeSeconds;

    @Column(name = "user_triggered")
    private Boolean userTriggered;

    @Column(name = "triggered_by")
    private UUID triggeredBy;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "retry_of_execution_id")
    private UUID retryOfExecutionId;

    @Column(name = "execution_output")
    private String executionOutput;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "execution_result")
    private JsonNode executionResult;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "completion_percentage", precision = 5, scale = 2)
    private BigDecimal completionPercentage;

    @Column(name = "items_processed")
    private Integer itemsProcessed;

    @Column(name = "items_succeeded")
    private Integer itemsSucceeded;

    @Column(name = "items_failed")
    private Integer itemsFailed;

    @Column(name = "items_skipped")
    private Integer itemsSkipped;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "stack_trace")
    private String stackTrace;

    @Column(name = "exit_code")
    private Integer exitCode;

    @Column(name = "worker_id", length = 100)
    private String workerId;

    @Column(name = "resource_cpu_percent", precision = 5, scale = 2)
    private BigDecimal resourceCpuPercent;

    @Column(name = "resource_memory_mb")
    private Integer resourceMemoryMb;

    @Column(name = "resource_disk_mb")
    private Integer resourceDiskMb;

    @Column(name = "log_file_path", length = 500)
    private String logFilePath;

    @Column(name = "execution_node", length = 100)
    private String executionNode;

    @Column(name = "next_scheduled_run")
    private OffsetDateTime nextScheduledRun;

    @Column(name = "notifications_sent")
    private Boolean notificationsSent;

    @Column(name = "archived")
    private Boolean archived;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
