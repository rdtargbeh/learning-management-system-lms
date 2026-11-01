package learning_management_system.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents an attachment associated with a message.
 */

@Data
@Entity
@Table(name = "message_attachment")
public class MessageAttachment {

    /** Unique identifier for the attachment. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id", nullable = false, updatable = false)
    private Long attachmentId;

    /** The message this attachment belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    /** The name of the attached file. */
    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    /** MIME type of the file (e.g., image/png, application/pdf). */
    @Column(name = "file_type", length = 50, nullable = false)
    private String fileType;

    /** Size of the file in bytes. */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /** URL or path to access the attachment. */
    @Column(name = "url", length = 255, nullable = false)
    private String url;

    /** Metadata for extensibility (e.g., expiration, custom tags). */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /** Timestamp for when the attachment was uploaded. */
    @Column(name = "date_uploaded", nullable = false)
    private LocalDateTime dateUploaded = LocalDateTime.now();
}
//
