package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AcademicRank;
import learning_management_system.backend.enums.EmployeeType;
import learning_management_system.backend.enums.EmploymentType;
import learning_management_system.backend.enums.StaffCategory;
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
 * Table <b>employee_profile</b> (Identity, Accounts & Profiles domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "employee_profile", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class EmployeeProfile implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "employee_profile_id", updatable = false)
    private UUID employeeProfileId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "employee_number", length = 50)
    private String employeeNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, length = 30)
    private EmployeeType employeeType;

    @Column(name = "org_unit_id")
    private UUID orgUnitId;

    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 30)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_category", length = 30)
    private StaffCategory staffCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_rank", length = 30)
    private AcademicRank academicRank;

    @Column(name = "office_location", length = 255)
    private String officeLocation;

    @Column(name = "office_phone", length = 30)
    private String officePhone;

    @Column(name = "office_hours_note")
    private String officeHoursNote;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expertise_areas")
    private JsonNode expertiseAreas;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "degrees")
    private JsonNode degrees;

    @Column(name = "research_interests")
    private String researchInterests;

    @Column(name = "max_courses_per_term")
    private Integer maxCoursesPerTerm;

    @Column(name = "date_hired")
    private LocalDate dateHired;

    @Column(name = "date_separated")
    private LocalDate dateSeparated;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
