package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.FeatureKey;
import learning_management_system.backend.enums.InteractionStatus;
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
 * Table <b>ai_interaction</b> (AI Features domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "ai_interaction", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AiInteraction implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_interaction_id", updatable = false)
    private UUID aiInteractionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "course_id")
    private UUID courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_key", nullable = false, length = 50)
    private FeatureKey featureKey;

    @Column(name = "model_provider", length = 50)
    private String modelProvider;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "entity_type", length = 30)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_status", nullable = false, length = 20)
    private InteractionStatus interactionStatus;

    @Column(name = "output_accepted")
    private Boolean outputAccepted;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "safety_flags")
    private JsonNode safetyFlags;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
