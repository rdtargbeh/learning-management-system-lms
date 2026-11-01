package learning_management_system.backend.entity;


import jakarta.persistence.*;
import learning_management_system.backend.enums.EbookAnnotationType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents an annotation (note or highlight) on an ebook or chapter.
 */

@Data
@Entity
@Table(name = "ebook_annotation")
public class EbookAnnotation {

    /** Unique identifier for the annotation. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "annotation_id", nullable = false, updatable = false)
    private Long annotationId;

    /** Many-to-One relationship with the User entity. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Many-to-One relationship with the Ebook entity. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ebook_id")
    private Ebook ebook;

    /** Many-to-One relationship with the EbookChapter entity. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private EbookChapter chapter;

    /** Range of text highlighted (start and end positions). */
    @Column(name = "highlight_range", columnDefinition = "TEXT")
    private String highlightRange;

    /** Type of annotation (e.g., NOTE, HIGHLIGHT). */
    @Enumerated(EnumType.STRING)
    @Column(name = "annotation_type", nullable = false, length = 20)
    private EbookAnnotationType annotationType;

    /** Content of the annotation or note. */
    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    /** Metadata for extensibility. */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /** Timestamp for when the annotation was created. */
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    /** Timestamp for when the annotation was last updated. */
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate = LocalDateTime.now();

    // Getters and setters omitted for brevity
}

