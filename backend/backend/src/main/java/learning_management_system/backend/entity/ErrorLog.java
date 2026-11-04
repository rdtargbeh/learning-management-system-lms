package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ErrorCategory;
import learning_management_system.backend.enums.ErrorSeverity;
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
 * Table <b>error_log</b> (System Administration & Maintenance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "error_log", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ErrorLog implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "error_id", updatable = false)
    private UUID errorId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "error_type", nullable = false, length = 100)
    private String errorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_category", nullable = false, length = 50)
    private ErrorCategory errorCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_severity", nullable = false, length = 50)
    private ErrorSeverity errorSeverity;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @Column(name = "error_detail")
    private String errorDetail;

    @Column(name = "stack_trace")
    private String stackTrace;

    @Column(name = "source_file", length = 255)
    private String sourceFile;

    @Column(name = "source_line")
    private Integer sourceLine;

    @Column(name = "source_function", length = 255)
    private String sourceFunction;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "request_id", length = 255)
    private String requestId;

    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    @Column(name = "related_entity_type", length = 100)
    private String relatedEntityType;

    @Column(name = "related_entity_id", length = 255)
    private String relatedEntityId;

    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "http_url", length = 500)
    private String httpUrl;

    @Column(name = "http_referer", length = 500)
    private String httpReferer;

    @Column(name = "user_agent")
    private String userAgent;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "source_ip")
    private InetAddress sourceIp;

    @Column(name = "environment", length = 50)
    private String environment;

    @Column(name = "application_version", length = 50)
    private String applicationVersion;

    @Column(name = "database_version", length = 50)
    private String databaseVersion;

    @Column(name = "is_reported")
    private Boolean isReported;

    @Column(name = "is_resolved")
    private Boolean isResolved;

    @Column(name = "resolved_date")
    private OffsetDateTime resolvedDate;

    @Column(name = "resolution_notes")
    private String resolutionNotes;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "error_frequency")
    private Integer errorFrequency;

    @Column(name = "last_occurrence")
    private OffsetDateTime lastOccurrence;

    @Column(name = "notifications_sent")
    private Boolean notificationsSent;

    @Column(name = "ticket_number", length = 100)
    private String ticketNumber;

    @Column(name = "escalation_level")
    private Integer escalationLevel;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
