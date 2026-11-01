package learning_management_system.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Tracks a student's progress through the chapters of an ebook.
 */
@Data
@Entity
@Table(name = "ebook_student_chapter_progress")
public class EbookStudentChapterProgress {

    /** Unique identifier for the progress record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id", nullable = false, updatable = false)
    private Long progressId;

    /** Many-to-One relationship with the Student entity. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /** Many-to-One relationship with the EbookChapter entity. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private EbookChapter chapter;

    /** Indicates whether the chapter is completed. */
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    /** Timestamp for when the chapter was marked as completed. */
    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    /** Score obtained in the chapter assessment (if applicable). */
    @Column(name = "assessment_score")
    private Double assessmentScore;

    /** Number of attempts made for the assessment. */
    @Column(name = "attempts_made")
    private Integer attemptsMade = 0;

    /** Indicates whether the chapter is unlocked for the student. */
    @Column(name = "is_unlocked", nullable = false)
    private Boolean isUnlocked = false;

    /** Total time spent by the student on the chapter. */
    @Column(name = "time_spent_seconds")
    private Long timeSpentSeconds = 0L;

    /** Metadata for extensibility. */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // Getters and setters omitted for brevity
}
