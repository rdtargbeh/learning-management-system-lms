package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LibraryType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>course_library</b> (Knowledge Base & Institutional Content domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "course_library", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class CourseLibrary implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "library_id", updatable = false)
    private UUID libraryId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "library_name", nullable = false, length = 255)
    private String libraryName;

    @Column(name = "library_description")
    private String libraryDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "library_type", nullable = false, length = 50)
    private LibraryType libraryType;

    @Column(name = "subject_area", length = 100)
    private String subjectArea;

    @Column(name = "curator_id")
    private UUID curatorId;

    @Column(name = "total_courses")
    private Integer totalCourses;

    @Column(name = "featured_courses")
    private Integer featuredCourses;

    @Column(name = "average_rating", precision = 3, scale = 1)
    private BigDecimal averageRating;

    @Column(name = "total_enrollments")
    private Integer totalEnrollments;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "requires_approval")
    private Boolean requiresApproval;

    @Column(name = "approval_process")
    private String approvalProcess;

    @Column(name = "distribution_method", length = 50)
    private String distributionMethod;

    @Column(name = "update_frequency", length = 50)
    private String updateFrequency;

    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

    @Column(name = "library_guidelines")
    private String libraryGuidelines;

    @Column(name = "quality_standards")
    private String qualityStandards;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
