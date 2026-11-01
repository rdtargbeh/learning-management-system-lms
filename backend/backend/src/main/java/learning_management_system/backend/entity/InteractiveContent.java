package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.InteractionType;
import learning_management_system.backend.enums.LaunchMethod;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>interactive_content</b> (Learning Content Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "interactive_content", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class InteractiveContent implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "interactive_id", updatable = false)
    private UUID interactiveId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", length = 50)
    private InteractionType interactionType;

    @Column(name = "interaction_url", length = 500)
    private String interactionUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "launch_method", length = 50)
    private LaunchMethod launchMethod;

    @Column(name = "lti_tool_id", length = 255)
    private String ltiToolId;

    @Column(name = "scorm_package_id", length = 255)
    private String scormPackageId;

    @Column(name = "allows_multiple_attempts")
    private Boolean allowsMultipleAttempts;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "interaction_instructions")
    private String interactionInstructions;

    @Column(name = "success_criteria")
    private String successCriteria;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
