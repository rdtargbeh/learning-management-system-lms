package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Save a comment to your library. */
public record CreateCommentLibraryItemRequest(@NotBlank @Size(max = 5000) String commentText) {}
