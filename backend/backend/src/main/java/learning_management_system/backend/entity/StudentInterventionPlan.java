package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.InterventionType;
import learning_management_system.backend.enums.RiskLevel;
import learning_management_system.backend.enums.StudentInterventionPlanStatus;
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
 * Table <b>student_intervention_plan</b> (Academic Advising & Student Support domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "student_intervention_plan", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class StudentInterventionPlan implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "plan_id", updatable = false)
    private UUID planId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "advisor_id", nullable = false)
    private UUID advisorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "intervention_type", nullable = false, length = 50)
    private InterventionType interventionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 50)
    private RiskLevel riskLevel;

    @Column(name = "intervention_title", nullable = false, length = 255)
    private String interventionTitle;

    @Column(name = "intervention_description")
    private String interventionDescription;

    @Column(name = "target_metric", length = 100)
    private String targetMetric;

    @Column(name = "target_value", precision = 8, scale = 2)
    private BigDecimal targetValue;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_status", nullable = false, length = 50)
    private StudentInterventionPlanStatus planStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "monitored_metrics")
    private JsonNode monitoredMetrics;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "interventions_assigned")
    private JsonNode interventionsAssigned;

    @Column(name = "check_in_frequency", length = 50)
    private String checkInFrequency;

    @Column(name = "last_check_in")
    private LocalDate lastCheckIn;

    @Column(name = "next_check_in")
    private LocalDate nextCheckIn;

    @Column(name = "effectiveness_rating", precision = 3, scale = 1)
    private BigDecimal effectivenessRating;

    @Column(name = "effectiveness_notes")
    private String effectivenessNotes;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
