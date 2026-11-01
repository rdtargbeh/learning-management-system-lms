package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ProgramType;
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
 * Table <b>mentorship_program</b> (Academic Advising & Student Support domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "mentorship_program", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class MentorshipProgram implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "program_id", updatable = false)
    private UUID programId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "program_name", nullable = false, length = 255)
    private String programName;

    @Column(name = "program_description")
    private String programDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "program_type", nullable = false, length = 50)
    private ProgramType programType;

    @Column(name = "mentor_qualifications")
    private String mentorQualifications;

    @Column(name = "mentee_requirements")
    private String menteeRequirements;

    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "current_matches")
    private Integer currentMatches;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "meeting_frequency", length = 50)
    private String meetingFrequency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "program_goals")
    private JsonNode programGoals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "success_metrics")
    private JsonNode successMetrics;

    @Column(name = "coordinator_id")
    private UUID coordinatorId;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
