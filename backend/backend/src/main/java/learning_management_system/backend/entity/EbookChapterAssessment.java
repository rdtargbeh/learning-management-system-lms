package learning_management_system.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Represents an assessment linked to a specific chapter in an ebook.
 */

@Data
@Entity
@Table(name = "ebook_chapter_assessment")
public class EbookChapterAssessment {

    /** Unique identifier for the assessment. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_id", nullable = false, updatable = false)
    private Long assessmentId;

    /** Many-to-One relationship with the EbookChapter entity. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private EbookChapter chapter;

    /** JSON or related table to store questions and options. */
    @Column(name = "questions", columnDefinition = "TEXT")
    private String questions;

    /** Minimum score required to pass the assessment. */
    @Column(name = "passing_score", nullable = false)
    private Double passingScore;

    /** Indicates if the assessment is required to unlock the next chapter. */
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;

    /** Maximum number of attempts allowed for the assessment. */
    @Column(name = "max_attempts", nullable = true)
    private Integer maxAttempts;

    /** Optional time limit (in minutes) for completing the assessment. */
    @Column(name = "time_limit_minutes", nullable = true)
    private Integer timeLimitMinutes;

    /** Metadata for extensibility. */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

// Getters and setters omitted for brevity

}