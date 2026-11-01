package learning_management_system.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a discussion thread for an ebook or a specific chapter.
 */

@Data
@Entity
@Table(name = "ebook_discussion")
public class EbookDiscussion {

    /** Unique identifier for the discussion. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discussion_id", nullable = false, updatable = false)
    private Long discussionId;

    /** Title of the discussion thread. */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** Initial content or description of the discussion. */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** Many-to-One relationship with the Ebook entity. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ebook_id")
    private Ebook ebook;

    /** Many-to-One relationship with the EbookChapter entity. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private EbookChapter chapter;

    /** Many-to-One relationship with the User entity for the discussion creator. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /** Tags for categorization and filtering. */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    /** Metadata for extensibility. */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /** Timestamp for when the discussion was created. */
    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated = LocalDateTime.now();

    /** Timestamp for when the discussion was last updated. */
    @Column(name = "date_updated", nullable = false)
    private LocalDateTime dateUpdated = LocalDateTime.now();

    // Getters and setters omitted for brevity
}

