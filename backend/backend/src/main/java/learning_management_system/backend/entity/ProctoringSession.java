package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ProctoringSessionReviewStatus;
import learning_management_system.backend.enums.ProctoringSessionStatus;
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
 * Table <b>proctoring_session</b> (Assessment & Testing domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "proctoring_session", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ProctoringSession implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "proctoring_session_id", updatable = false)
    private UUID proctoringSessionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "provider_session_ref", length = 255)
    private String providerSessionRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false, length = 20)
    private ProctoringSessionStatus sessionStatus;

    @Column(name = "date_start")
    private OffsetDateTime dateStart;

    @Column(name = "date_end")
    private OffsetDateTime dateEnd;

    @Column(name = "recording_file_id")
    private UUID recordingFileId;

    @Column(name = "violation_count", nullable = false)
    private Integer violationCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "violations")
    private JsonNode violations;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", length = 20)
    private ProctoringSessionReviewStatus reviewStatus;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "date_reviewed")
    private OffsetDateTime dateReviewed;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
