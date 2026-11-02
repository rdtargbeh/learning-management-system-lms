package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CommentLibraryItemDto;
import learning_management_system.backend.dto.CreateCommentLibraryItemRequest;
import learning_management_system.backend.service.CommentLibraryService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** The caller's reusable feedback comments. */
@RestController
@RequestMapping("/api/v1/comment-library")
public class CommentLibraryController {
    private final CommentLibraryService library;

    public CommentLibraryController(CommentLibraryService library) { this.library = library; }

    @GetMapping
    public List<CommentLibraryItemDto> mine() { return library.mine(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentLibraryItemDto add(@Valid @RequestBody CreateCommentLibraryItemRequest r) { return library.add(r); }

    @PostMapping("/{libraryItemId}/use")
    public CommentLibraryItemDto use(@PathVariable UUID libraryItemId) { return library.use(libraryItemId); }

    @DeleteMapping("/{libraryItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID libraryItemId) { library.delete(libraryItemId); }
}
