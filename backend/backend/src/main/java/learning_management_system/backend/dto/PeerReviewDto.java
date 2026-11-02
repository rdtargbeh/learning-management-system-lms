package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AssignmentPeerReviewStatus;

/**
 * A peer review (table assignment_peer_review). For the reviewer it carries the work to review; names are hidden
 * when the review is anonymous.
 */
public record PeerReviewDto(UUID peerReviewId, UUID assignmentId, UUID reviewerId, String reviewerName, UUID submissionId,
                            UUID revieweeId, String revieweeName, String submissionText, String submissionUrl,
                            AssignmentPeerReviewStatus reviewStatus, String reviewText, Integer reviewRating,
                            OffsetDateTime dateAssigned, OffsetDateTime dateSubmitted, Boolean anonymousReview) {}
