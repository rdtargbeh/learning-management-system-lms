package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.MetadataSchema;
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
 * Table <b>learning_object_metadata</b> (Knowledge Base & Institutional Content domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "learning_object_metadata", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class LearningObjectMetadata implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "metadata_id", updatable = false)
    private UUID metadataId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "object_id", nullable = false)
    private UUID objectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metadata_schema", nullable = false, length = 100)
    private MetadataSchema metadataSchema;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dublin_core_data")
    private JsonNode dublinCoreData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lom_data")
    private JsonNode lomData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scorm_metadata")
    private JsonNode scormMetadata;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_metadata")
    private JsonNode customMetadata;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "keywords")
    private JsonNode keywords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "competencies")
    private JsonNode competencies;

    @Column(name = "prerequisites")
    private String prerequisites;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "learning_objectives")
    private JsonNode learningObjectives;

    @Column(name = "interactivity_level", length = 50)
    private String interactivityLevel;

    @Column(name = "semantic_density", length = 50)
    private String semanticDensity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "intended_end_user_role")
    private JsonNode intendedEndUserRole;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context")
    private JsonNode context;

    @Column(name = "typical_age_range", length = 50)
    private String typicalAgeRange;

    @Column(name = "rights_and_restrictions")
    private String rightsAndRestrictions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "creation_context")
    private JsonNode creationContext;

    @Column(name = "educational_description")
    private String educationalDescription;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
