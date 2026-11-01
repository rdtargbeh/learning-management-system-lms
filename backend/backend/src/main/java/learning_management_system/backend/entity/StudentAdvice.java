package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AdviceCategory;
import learning_management_system.backend.enums.OutcomeStatus;
import learning_management_system.backend.enums.StudentAdviceStatus;
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
 * Table <b>student_advice</b> (Academic Advising & Student Support domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "student_advice", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class StudentAdvice implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "advice_id", updatable = false)
    private UUID adviceId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "advisor_id", nullable = false)
    private UUID advisorId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "advice_category", nullable = false, length = 50)
    private AdviceCategory adviceCategory;

    @Column(name = "advice_title", nullable = false, length = 255)
    private String adviceTitle;

    @Column(name = "advice_description", nullable = false)
    private String adviceDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "advice_detail")
    private JsonNode adviceDetail;

    @Column(name = "is_critical")
    private Boolean isCritical;

    @Column(name = "implementation_deadline")
    private LocalDate implementationDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private StudentAdviceStatus status;

    @Column(name = "student_acknowledged")
    private Boolean studentAcknowledged;

    @Column(name = "date_acknowledged")
    private OffsetDateTime dateAcknowledged;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome_status", length = 50)
    private OutcomeStatus outcomeStatus;

    @Column(name = "outcome_date")
    private OffsetDateTime outcomeDate;

    @Column(name = "outcome_notes")
    private String outcomeNotes;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
