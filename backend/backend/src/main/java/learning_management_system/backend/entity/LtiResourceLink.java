package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
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
 * Table <b>lti_resource_link</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "lti_resource_link", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class LtiResourceLink implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "resource_link_id", updatable = false)
    private UUID resourceLinkId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "registration_id", nullable = false)
    private UUID registrationId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "content_id")
    private UUID contentId;

    @Column(name = "assignment_id")
    private UUID assignmentId;

    @Column(name = "grade_item_id")
    private UUID gradeItemId;

    @Column(name = "lti_resource_link_ref", nullable = false, length = 255)
    private String ltiResourceLinkRef;

    @Column(name = "link_title", length = 255)
    private String linkTitle;

    @Column(name = "launch_url", length = 1000)
    private String launchUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_parameters")
    private JsonNode customParameters;

    @Column(name = "ags_line_item_url", length = 1000)
    private String agsLineItemUrl;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
