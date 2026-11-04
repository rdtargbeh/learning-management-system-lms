package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.GoalCategory;
import learning_management_system.backend.enums.GoalStatus;
import learning_management_system.backend.enums.GoalType;
import learning_management_system.backend.enums.StudentGoalPriority;
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
 * Table <b>student_goal</b> (Academic Advising & Student Support domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "student_goal", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class StudentGoal implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "goal_id", updatable = false)
    private UUID goalId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "advisor_id")
    private UUID advisorId;

    @Column(name = "goal_title", nullable = false, length = 255)
    private String goalTitle;

    @Column(name = "goal_description", nullable = false)
    private String goalDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_category", nullable = false, length = 50)
    private GoalCategory goalCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 50)
    private GoalType goalType;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 50)
    private StudentGoalPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_status", nullable = false, length = 50)
    private GoalStatus goalStatus;

    @Column(name = "measurable_criteria")
    private String measurableCriteria;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "success_metrics")
    private JsonNode successMetrics;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "related_course_ids")
    private JsonNode relatedCourseIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "related_resources")
    private JsonNode relatedResources;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
