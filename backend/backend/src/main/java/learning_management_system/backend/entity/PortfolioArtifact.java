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
 * Table <b>portfolio_artifact</b> (ePortfolio & Catalog domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "portfolio_artifact", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class PortfolioArtifact implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "portfolio_artifact_id", updatable = false)
    private UUID portfolioArtifactId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "portfolio_section_id", nullable = false)
    private UUID portfolioSectionId;

    @Column(name = "artifact_title", nullable = false, length = 255)
    private String artifactTitle;

    @Column(name = "reflection")
    private String reflection;

    @Column(name = "submission_id")
    private UUID submissionId;

    @Column(name = "file_id")
    private UUID fileId;

    @Column(name = "student_badge_id")
    private UUID studentBadgeId;

    @Column(name = "external_url", length = 1000)
    private String externalUrl;

    @Column(name = "outcome_id")
    private UUID outcomeId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
