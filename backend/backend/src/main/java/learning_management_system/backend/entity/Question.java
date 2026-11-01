package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.PartialCreditMode;
import learning_management_system.backend.enums.QuestionBloomLevel;
import learning_management_system.backend.enums.QuestionDifficultyLevel;
import learning_management_system.backend.enums.QuestionType;
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
 * Table <b>question</b> (Assessment & Testing domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "question", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Question implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "question_id", updatable = false)
    private UUID questionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "assessment_id")
    private UUID assessmentId;

    @Column(name = "bank_id")
    private UUID bankId;

    @Column(name = "parent_question_id")
    private UUID parentQuestionId;

    @Column(name = "question_number")
    private Integer questionNumber;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 50)
    private QuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 50)
    private QuestionDifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "bloom_level", length = 50)
    private QuestionBloomLevel bloomLevel;

    @Column(name = "points", precision = 8, scale = 2)
    private BigDecimal points;

    @Enumerated(EnumType.STRING)
    @Column(name = "partial_credit_mode", nullable = false, length = 20)
    private PartialCreditMode partialCreditMode;

    @Column(name = "correct_answer")
    private String correctAnswer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "correct_answers")
    private JsonNode correctAnswers;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "interaction_data")
    private JsonNode interactionData;

    @Column(name = "feedback_if_correct")
    private String feedbackIfCorrect;

    @Column(name = "feedback_if_incorrect")
    private String feedbackIfIncorrect;

    @Column(name = "explanation")
    private String explanation;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "is_randomized")
    private Boolean isRandomized;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
