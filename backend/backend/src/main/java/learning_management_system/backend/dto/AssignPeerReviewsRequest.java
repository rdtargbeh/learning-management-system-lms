package learning_management_system.backend.dto;

import jakarta.validation.constraints.Positive;

/**
 * Hand out peer reviews: each student who submitted reviews this many classmates (default: the assignment's
 * peerReviewCount). anonymous hides names both ways.
 */
public record AssignPeerReviewsRequest(@Positive Integer reviewsPerStudent, Boolean anonymous) {}
