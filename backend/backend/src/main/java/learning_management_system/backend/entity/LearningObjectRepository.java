package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.RepositoryType;
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
 * Table <b>learning_object_repository</b> (Knowledge Base & Institutional Content domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "learning_object_repository", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class LearningObjectRepository implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "repository_id", updatable = false)
    private UUID repositoryId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "repository_name", nullable = false, length = 255)
    private String repositoryName;

    @Column(name = "repository_description")
    private String repositoryDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "repository_type", nullable = false, length = 50)
    private RepositoryType repositoryType;

    @Column(name = "repository_url", length = 500)
    private String repositoryUrl;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "curator_id")
    private UUID curatorId;

    @Column(name = "total_objects")
    private Integer totalObjects;

    @Column(name = "featured_objects")
    private Integer featuredObjects;

    @Column(name = "total_downloads")
    private Integer totalDownloads;

    @Column(name = "average_rating", precision = 3, scale = 1)
    private BigDecimal averageRating;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "indexing_enabled")
    private Boolean indexingEnabled;

    @Column(name = "last_indexed")
    private OffsetDateTime lastIndexed;

    @Column(name = "metadata_schema", length = 100)
    private String metadataSchema;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "repository_metadata")
    private JsonNode repositoryMetadata;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
