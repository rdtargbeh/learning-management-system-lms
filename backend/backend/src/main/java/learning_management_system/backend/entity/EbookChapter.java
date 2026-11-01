package learning_management_system.backend.entity;

import jakarta.persistence.*;
import lombok.Data;


/**
 * Represents a chapter in an ebook.
 */

@Data
@Entity
@Table(name = "ebook_chapter")
public class EbookChapter {

    /** Unique identifier for the chapter. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id", nullable = false, updatable = false)
    private Long chapterId;

    /** Many-to-One relationship with the Ebook entity. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ebook_id", nullable = false)
    private Ebook ebook;

    /** Title of the chapter. */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** Numeric order of the chapter in the ebook. */
    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;

    /** Path or URL to the chapter's content. */
    @Column(name = "content_path", nullable = false, length = 500)
    private String contentPath;

    /** Brief summary of the chapter. */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /** Indicates whether the chapter requires sequential unlocking. */
    @Column(name = "is_sequential", nullable = false)
    private Boolean isSequential = true;

    /** Indicates if the chapter includes an assessment. */
    @Column(name = "has_assessment", nullable = false)
    private Boolean hasAssessment = false;

    /** Estimated time (in minutes) to complete the chapter. */
    @Column(name = "estimated_read_time", nullable = true)
    private Integer estimatedReadTime;

    /** Metadata for extensibility. */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // Getters and setters omitted for brevity
}

