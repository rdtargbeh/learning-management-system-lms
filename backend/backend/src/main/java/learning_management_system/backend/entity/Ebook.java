package learning_management_system.backend.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents an ebook resource in the LMS.
 */

@Data
@Entity
@Table(name = "ebook")
public class Ebook {

    /** Unique identifier for the ebook. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ebook_id", nullable = false, updatable = false)
    private Long ebookId;

    /** Title of the ebook. */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** Author(s) of the ebook. */
    @Column(name = "author", nullable = false, length = 255)
    private String author;

    /** Description or summary of the ebook. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Path or URL to the stored ebook file. */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /** Format of the ebook (e.g., PDF, EPUB). */
    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    /** Indicates if the ebook supports interactive content. */
    @Column(name = "is_interactive", nullable = false)
    private Boolean isInteractive = false;

    /** Many-to-One relationship with the Course entity. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** Tags for categorization and search. */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    /** Metadata for extensibility. */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /** Timestamp for when the ebook was uploaded. */
    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate = LocalDateTime.now();

    /** Timestamp for when the ebook was last updated. */
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate = LocalDateTime.now();

    // Getters and setters omitted for brevity
}
