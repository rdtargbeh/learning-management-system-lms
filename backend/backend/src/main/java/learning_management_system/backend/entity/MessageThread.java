package learning_management_system.backend.entity;

import jakarta.persistence.*;
import learning_management_system.backend.enums.MessageAssociatedEntityType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a conversation thread in the messaging system.
 */
@Data
@Entity
@Table(name = "message_thread")
public class MessageThread {

    /** Unique identifier for the thread. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thread_id", nullable = false, updatable = false)
    private Long threadId;

    /** Boolean indicating if the thread is a group conversation. */
    @Column(name = "is_group", nullable = false)
    private Boolean isGroup = false;

    /** The users participating in this thread. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "thread_participants",
            joinColumns = @JoinColumn(name = "thread_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    /** Type of the associated LMS entity (e.g., Course, Assignment). */
    @Enumerated(EnumType.STRING)
    @Column(name = "associated_entity_type", nullable = true)
    private MessageAssociatedEntityType associatedEntityType;

    /** ID of the associated LMS entity. */
    @Column(name = "associated_entity_id", nullable = true)
    private Long associatedEntityId;

    /** Timestamp of the last message sent in this thread. */
    @Column(name = "last_message_at", nullable = true)
    private LocalDateTime lastMessageAt;

    /** Boolean indicating if the thread is pinned by the user. */
    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    /** Boolean indicating if the thread is archived. */
    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;

    /** Metadata for extensibility (e.g., custom settings, tags). */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
