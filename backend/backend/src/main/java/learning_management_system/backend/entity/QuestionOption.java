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
 * Table <b>question_option</b> (Assessment & Testing domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "question_option", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class QuestionOption implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "option_id", updatable = false)
    private UUID optionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "option_letter", length = 5)
    private String optionLetter;

    @Column(name = "option_text", nullable = false)
    private String optionText;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "feedback")
    private String feedback;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
