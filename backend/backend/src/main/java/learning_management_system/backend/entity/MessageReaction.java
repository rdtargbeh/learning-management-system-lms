package learning_management_system.backend.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents an emoji-based reaction to a message.
 */

@Data
@Entity
@Table(name = "message_reaction")
public class MessageReaction {

    /** Unique identifier for the reaction. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id", nullable = false, updatable = false)
    private Long reactionId;

    /** The message this reaction is associated with. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    /** The user who added the reaction. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The emoji used for the reaction. */
    @Column(name = "emoji", length = 50, nullable = false)
    private String emoji;

    /** The timestamp for when the reaction was made. */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    /** Metadata for extensibility. */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}

