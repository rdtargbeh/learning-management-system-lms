package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ProctoringType;
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
 * Table <b>test_security</b> (Assessment & Testing domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "test_security", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class TestSecurity implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "security_id", updatable = false)
    private UUID securityId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @Column(name = "require_proctoring")
    private Boolean requireProctoring;

    @Enumerated(EnumType.STRING)
    @Column(name = "proctoring_type", length = 50)
    private ProctoringType proctoringType;

    @Column(name = "proctoring_provider", length = 100)
    private String proctoringProvider;

    @Column(name = "require_webcam")
    private Boolean requireWebcam;

    @Column(name = "require_microphone")
    private Boolean requireMicrophone;

    @Column(name = "lock_questions_after_answering")
    private Boolean lockQuestionsAfterAnswering;

    @Column(name = "disable_copy_paste")
    private Boolean disableCopyPaste;

    @Column(name = "disable_right_click")
    private Boolean disableRightClick;

    @Column(name = "disable_printing")
    private Boolean disablePrinting;

    @Column(name = "fullscreen_required")
    private Boolean fullscreenRequired;

    @Column(name = "window_security")
    private Boolean windowSecurity;

    @Column(name = "require_unique_session_id")
    private Boolean requireUniqueSessionId;

    @Column(name = "ip_restriction_enabled")
    private Boolean ipRestrictionEnabled;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_ip_addresses")
    private JsonNode allowedIpAddresses;

    @Column(name = "require_authentication")
    private Boolean requireAuthentication;

    @Column(name = "authentication_method", length = 50)
    private String authenticationMethod;

    @Column(name = "access_code_hash", length = 255)
    private String accessCodeHash;

    @Column(name = "require_lockdown_browser", nullable = false)
    private Boolean requireLockdownBrowser;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
