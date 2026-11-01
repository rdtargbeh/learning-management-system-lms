package learning_management_system.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an individual message in a messaging thread.
 */
@Data
@Entity
@Table(name = "message")
public class Message {

    /** Unique identifier for the message. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", nullable = false, updatable = false)
    private Long messageId;

    /** The thread this message belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private MessageThread thread;

    /** The user who sent the message. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /** The text content of the message. */
    @Column(name = "content", columnDefinition = "TEXT", nullable = true)
    private String content;

    /** Attachments associated with the message. */
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageAttachment> attachments = new ArrayList<>();

    /** Reactions associated with the message. */
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageReaction> reactions = new ArrayList<>();

    /** Boolean indicating if the message has been read. */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /** The timestamp for when the message was sent. */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    /** Metadata for extensibility. */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
