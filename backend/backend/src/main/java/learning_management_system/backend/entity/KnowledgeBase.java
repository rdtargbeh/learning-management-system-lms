package learning_management_system.backend.entity;

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

/**
 * Table <b>knowledge_base</b> (Knowledge Base & Institutional Content domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "knowledge_base", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class KnowledgeBase implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "kb_id", updatable = false)
    private UUID kbId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "kb_name", nullable = false, length = 255)
    private String kbName;

    @Column(name = "kb_description")
    private String kbDescription;

    @Column(name = "kb_category", length = 100)
    private String kbCategory;

    @Column(name = "kb_icon_url", length = 500)
    private String kbIconUrl;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "requires_authentication")
    private Boolean requiresAuthentication;

    @Column(name = "featured")
    private Boolean featured;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "search_enabled")
    private Boolean searchEnabled;

    @Column(name = "ai_powered_search")
    private Boolean aiPoweredSearch;

    @Column(name = "total_articles")
    private Integer totalArticles;

    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "maintainer_id")
    private UUID maintainerId;

    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "seo_keywords", length = 255)
    private String seoKeywords;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
