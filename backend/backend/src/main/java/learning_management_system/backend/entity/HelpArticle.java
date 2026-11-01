package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ArticleStatus;
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
 * Table <b>help_article</b> (Knowledge Base & Institutional Content domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "help_article", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class HelpArticle implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "article_id", updatable = false)
    private UUID articleId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "kb_id", nullable = false)
    private UUID kbId;

    @Column(name = "article_title", nullable = false, length = 255)
    private String articleTitle;

    @Column(name = "article_slug", length = 255)
    private String articleSlug;

    @Column(name = "article_content", nullable = false)
    private String articleContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "article_html")
    private JsonNode articleHtml;

    @Column(name = "article_category", length = 100)
    private String articleCategory;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "article_tags")
    private JsonNode articleTags;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "editor_id")
    private UUID editorId;

    @Column(name = "last_edited_at")
    private OffsetDateTime lastEditedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "article_status", nullable = false, length = 50)
    private ArticleStatus articleStatus;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Column(name = "published_date")
    private OffsetDateTime publishedDate;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @Column(name = "helpful_count")
    private Integer helpfulCount;

    @Column(name = "unhelpful_count")
    private Integer unhelpfulCount;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "search_keywords", length = 500)
    private String searchKeywords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "related_article_ids")
    private JsonNode relatedArticleIds;

    @Column(name = "attachments_count")
    private Integer attachmentsCount;

    @Column(name = "language_code", length = 10)
    private String languageCode;

    @Column(name = "version_number")
    private Integer versionNumber;

    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "estimated_read_time_minutes")
    private Integer estimatedReadTimeMinutes;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
