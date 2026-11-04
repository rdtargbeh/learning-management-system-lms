package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.TermType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>academic_term</b> (Academic Structure domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "academic_term", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AcademicTerm implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "term_id", updatable = false)
    private UUID termId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "calendar_id")
    private UUID calendarId;

    @Column(name = "term_name", nullable = false, length = 100)
    private String termName;

    @Column(name = "term_code", nullable = false, length = 50)
    private String termCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "term_type", nullable = false, length = 50)
    private TermType termType;

    @Column(name = "date_start", nullable = false)
    private LocalDate dateStart;

    @Column(name = "date_end", nullable = false)
    private LocalDate dateEnd;

    @Column(name = "date_census")
    private LocalDate dateCensus;

    @Column(name = "date_add_drop_deadline")
    private LocalDate dateAddDropDeadline;

    @Column(name = "date_withdraw_deadline")
    private LocalDate dateWithdrawDeadline;

    @Column(name = "date_grades_due")
    private LocalDate dateGradesDue;

    @Column(name = "sis_source_id", length = 255)
    private String sisSourceId;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
