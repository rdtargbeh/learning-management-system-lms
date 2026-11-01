package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AdoptionStatus;
import learning_management_system.backend.enums.FrameworkType;
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
 * Table <b>standards_framework</b> (Learning Standards & Compliance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "standards_framework", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class StandardsFramework implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "framework_id", updatable = false)
    private UUID frameworkId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "framework_name", nullable = false, length = 255)
    private String frameworkName;

    @Column(name = "framework_description")
    private String frameworkDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "framework_type", nullable = false, length = 50)
    private FrameworkType frameworkType;

    @Column(name = "framework_version", length = 50)
    private String frameworkVersion;

    @Column(name = "source_organization", length = 255)
    private String sourceOrganization;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "adoption_status", length = 50)
    private AdoptionStatus adoptionStatus;

    @Column(name = "adoption_date")
    private LocalDate adoptionDate;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "is_primary_framework")
    private Boolean isPrimaryFramework;

    @Column(name = "total_standards")
    private Integer totalStandards;

    @Column(name = "mapped_courses")
    private Integer mappedCourses;

    @Column(name = "mapped_content_count")
    private Integer mappedContentCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "framework_structure")
    private JsonNode frameworkStructure;

    @Column(name = "implementation_guidelines")
    private String implementationGuidelines;

    @Column(name = "is_required_by_accreditor")
    private Boolean isRequiredByAccreditor;

    @Column(name = "accreditor_name", length = 255)
    private String accreditorName;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
