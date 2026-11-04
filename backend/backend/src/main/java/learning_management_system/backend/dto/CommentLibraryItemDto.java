package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A saved comment a grader reuses (table comment_library_item). */
public record CommentLibraryItemDto(UUID libraryItemId, String commentText, Integer usageCount, OffsetDateTime dateCreated) {}
