package learning_management_system.backend.entity;


import jakarta.persistence.*;
import learning_management_system.backend.enums.SystemFeedbackContextEntityType;
import learning_management_system.backend.enums.SystemFeedbackStatus;
import learning_management_system.backend.enums.SystemFeedbackType;
import learning_management_system.backend.enums.VisibleTo;
import lombok.Data;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;

/**
 * Represents a feedback entry in the LMS.
 */
@CrossOrigin("*")
@Entity
@Table(name = "system_feedbacks")
public class SystemFeedback {

    /** Unique identifier for the feedback entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id", nullable = false, updatable = false)
    private Long feedbackId;

    /** Type of entity the feedback is related to (e.g., "Course," "Assignment"). */
    @Enumerated(EnumType.STRING)
    @Column(name = "context_entity_type", nullable = false, length = 50)
    private SystemFeedbackContextEntityType contextEntityType;

    /** ID of the entity the feedback is related to. */
    @Column(name = "context_entity_id", nullable = false)
    private Long contextEntityId;

    /** User who submitted the feedback. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    /** Type of feedback (e.g., COMMENT, REVIEW, SUGGESTION). */
    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 20)
    private SystemFeedbackType feedbackType;

    /** The actual feedback content. */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /** Numeric rating (e.g., 1–5 stars). */
    @Column(name = "rating")
    private Integer rating;

    /** Visibility of the feedback (e.g., PUBLIC, PRIVATE). */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private VisibleTo visibility;

    /** Boolean indicating whether the feedback is anonymous. */
    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = false;

    /** Response to the feedback. */
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    /** Status of the feedback (e.g., PENDING, ADDRESSED). */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SystemFeedbackStatus status = SystemFeedbackStatus.PENDING;

    /** Metadata for extensibility. */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /** Timestamp for when the feedback was submitted. */
    @Column(name = "date_submitted", nullable = false)
    private LocalDateTime dateSubmitted = LocalDateTime.now();

    /** Timestamp for when a response was added. */
    @Column(name = "date_responded")
    private LocalDateTime dateResponded;


    public SystemFeedback() {
    }

    public SystemFeedback(Long feedbackId, SystemFeedbackContextEntityType contextEntityType, Long contextEntityId, User submittedBy, SystemFeedbackType feedbackType, String content, Integer rating, VisibleTo visibility, Boolean isAnonymous, String response, SystemFeedbackStatus status, String metadata, LocalDateTime dateSubmitted, LocalDateTime dateResponded) {
        this.feedbackId = feedbackId;
        this.contextEntityType = contextEntityType;
        this.contextEntityId = contextEntityId;
        this.submittedBy = submittedBy;
        this.feedbackType = feedbackType;
        this.content = content;
        this.rating = rating;
        this.visibility = visibility;
        this.isAnonymous = isAnonymous;
        this.response = response;
        this.status = status;
        this.metadata = metadata;
        this.dateSubmitted = dateSubmitted;
        this.dateResponded = dateResponded;
    }

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public SystemFeedbackContextEntityType getContextEntityType() {
        return contextEntityType;
    }

    public void setContextEntityType(SystemFeedbackContextEntityType contextEntityType) {
        this.contextEntityType = contextEntityType;
    }

    public Long getContextEntityId() {
        return contextEntityId;
    }

    public void setContextEntityId(Long contextEntityId) {
        this.contextEntityId = contextEntityId;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }

    public SystemFeedbackType getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(SystemFeedbackType feedbackType) {
        this.feedbackType = feedbackType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public VisibleTo getVisibility() {
        return visibility;
    }

    public void setVisibility(VisibleTo visibility) {
        this.visibility = visibility;
    }

    public Boolean getAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        isAnonymous = anonymous;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public SystemFeedbackStatus getStatus() {
        return status;
    }

    public void setStatus(SystemFeedbackStatus status) {
        this.status = status;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(LocalDateTime dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public LocalDateTime getDateResponded() {
        return dateResponded;
    }

    public void setDateResponded(LocalDateTime dateResponded) {
        this.dateResponded = dateResponded;
    }
}
