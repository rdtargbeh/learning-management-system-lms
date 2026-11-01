package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.CompetencyLevel;
import learning_management_system.backend.enums.EducationalStandardType;
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
 * Table <b>educational_standard</b> (Learning Standards & Compliance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "educational_standard", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class EducationalStandard implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "standard_id", updatable = false)
    private UUID standardId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "standard_name", nullable = false, length = 255)
    private String standardName;

    @Column(name = "standard_code", length = 100)
    private String standardCode;

    @Column(name = "standard_description")
    private String standardDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "standard_type", nullable = false, length = 50)
    private EducationalStandardType standardType;

    @Column(name = "subject_area", length = 100)
    private String subjectArea;

    @Column(name = "grade_level", length = 50)
    private String gradeLevel;

    @Column(name = "academic_level", length = 50)
    private String academicLevel;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "source_organization", length = 255)
    private String sourceOrganization;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "alignment_difficulty", precision = 3, scale = 1)
    private BigDecimal alignmentDifficulty;

    @Column(name = "assessment_method", length = 100)
    private String assessmentMethod;

    @Column(name = "parent_standard_id")
    private UUID parentStandardId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "learning_outcomes")
    private JsonNode learningOutcomes;

    @Enumerated(EnumType.STRING)
    @Column(name = "competency_level", length = 50)
    private CompetencyLevel competencyLevel;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
