package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>grade_history</b> (Grading & Evaluation domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "grade_history", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class GradeHistory implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id", updatable = false)
    private UUID historyId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "entry_id", nullable = false)
    private UUID entryId;

    @Column(name = "previous_score", precision = 8, scale = 2)
    private BigDecimal previousScore;

    @Column(name = "new_score", precision = 8, scale = 2)
    private BigDecimal newScore;

    @Column(name = "previous_percentage", precision = 5, scale = 2)
    private BigDecimal previousPercentage;

    @Column(name = "new_percentage", precision = 5, scale = 2)
    private BigDecimal newPercentage;

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    @Column(name = "changed_by", nullable = false)
    private UUID changedBy;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "date_changed", nullable = false)
    private OffsetDateTime dateChanged;
}
