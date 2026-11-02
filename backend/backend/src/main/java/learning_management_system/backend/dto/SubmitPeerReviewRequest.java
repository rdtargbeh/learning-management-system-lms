package learning_management_system.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** The reviewer's comments and a 1-5 rating. */
public record SubmitPeerReviewRequest(@NotBlank String reviewText, @Min(1) @Max(5) Integer reviewRating) {}
