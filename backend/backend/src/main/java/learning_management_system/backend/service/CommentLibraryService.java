package learning_management_system.backend.service;

import learning_management_system.backend.dto.CommentLibraryItemDto;
import learning_management_system.backend.dto.CreateCommentLibraryItemRequest;

import java.util.List;
import java.util.UUID;

/** A grader's personal library of reusable feedback comments (Canvas comment library). */
public interface CommentLibraryService {

    List<CommentLibraryItemDto> mine();

    CommentLibraryItemDto add(CreateCommentLibraryItemRequest r);

    /** Counts a use, so the most used comments come first. */
    CommentLibraryItemDto use(UUID libraryItemId);

    void delete(UUID libraryItemId);
}
