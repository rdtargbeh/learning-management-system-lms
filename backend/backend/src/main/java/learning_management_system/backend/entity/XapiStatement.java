package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.XapiStatementObjectType;
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
 * Table <b>xapi_statement</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "xapi_statement", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class XapiStatement implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "statement_id", updatable = false)
    private UUID statementId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "actor_mbox", length = 255)
    private String actorMbox;

    @Column(name = "actor_account_name", length = 255)
    private String actorAccountName;

    @Column(name = "verb", nullable = false, length = 100)
    private String verb;

    @Column(name = "verb_display", length = 255)
    private String verbDisplay;

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false, length = 50)
    private XapiStatementObjectType objectType;

    @Column(name = "object_id", length = 500)
    private String objectId;

    @Column(name = "object_definition_name", length = 255)
    private String objectDefinitionName;

    @Column(name = "object_definition_description")
    private String objectDefinitionDescription;

    @Column(name = "result_completion")
    private Boolean resultCompletion;

    @Column(name = "result_success")
    private Boolean resultSuccess;

    @Column(name = "result_score", precision = 5, scale = 2)
    private BigDecimal resultScore;

    @Column(name = "result_response")
    private String resultResponse;

    @Column(name = "result_duration", length = 20)
    private String resultDuration;

    @Column(name = "context_course_id")
    private UUID contextCourseId;

    @Column(name = "context_session_id")
    private UUID contextSessionId;

    @Column(name = "context_activity_id", length = 500)
    private String contextActivityId;

    @Column(name = "context_language", length = 10)
    private String contextLanguage;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "stored", nullable = false)
    private OffsetDateTime stored;

    @Column(name = "version", length = 50)
    private String version;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "statement_data")
    private JsonNode statementData;

    @Column(name = "is_voided")
    private Boolean isVoided;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
