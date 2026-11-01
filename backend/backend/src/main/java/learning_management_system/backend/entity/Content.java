package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ContentAccessLevel;
import learning_management_system.backend.enums.EditingRoles;
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
 * Table <b>content</b> (Learning Content Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "content", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Content implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "content_id", updatable = false)
    private UUID contentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "type_id", nullable = false)
    private UUID typeId;

    @Column(name = "content_title", nullable = false, length = 255)
    private String contentTitle;

    @Column(name = "content_description")
    private String contentDescription;

    @Column(name = "content_body")
    private String contentBody;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_html")
    private JsonNode contentHtml;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Column(name = "is_front_page", nullable = false)
    private Boolean isFrontPage;

    @Enumerated(EnumType.STRING)
    @Column(name = "editing_roles", nullable = false, length = 30)
    private EditingRoles editingRoles;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", length = 50)
    private ContentAccessLevel accessLevel;

    @Column(name = "date_published")
    private OffsetDateTime datePublished;

    @Column(name = "date_start")
    private LocalDate dateStart;

    @Column(name = "date_end")
    private LocalDate dateEnd;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
