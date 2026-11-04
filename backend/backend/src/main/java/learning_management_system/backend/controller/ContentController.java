package learning_management_system.backend.controller;

import learning_management_system.backend.dto.ContentDto;
import learning_management_system.backend.dto.ContentProgressDto;
import learning_management_system.backend.dto.CreateContentRequest;
import learning_management_system.backend.dto.MoveContentRequest;
import learning_management_system.backend.dto.UpdateContentRequest;
import learning_management_system.backend.service.ContentService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Content items: pages, videos, files, links ... */
@RestController
@RequestMapping("/api/v1")
public class ContentController {
    private final ContentService content;

    public ContentController(ContentService content) { this.content = content; }

    @GetMapping("/courses/{courseId}/content")
    public List<ContentDto> list(@PathVariable UUID courseId, @RequestParam(required = false) String type) {
        return content.list(courseId, type);
    }

    @PostMapping("/courses/{courseId}/content")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentDto create(@PathVariable UUID courseId, @Valid @RequestBody CreateContentRequest r) { return content.create(courseId, r); }

    @GetMapping("/courses/{courseId}/front-page")
    public ContentDto frontPage(@PathVariable UUID courseId) { return content.frontPage(courseId); }

    @GetMapping("/content/{contentId}")
    public ContentDto get(@PathVariable UUID contentId) { return content.get(contentId); }

    @PatchMapping("/content/{contentId}")
    public ContentDto update(@PathVariable UUID contentId, @Valid @RequestBody UpdateContentRequest r) { return content.update(contentId, r); }

    @PostMapping("/content/{contentId}/publish")
    public ContentDto publish(@PathVariable UUID contentId) { return content.setPublished(contentId, true); }

    @PostMapping("/content/{contentId}/unpublish")
    public ContentDto unpublish(@PathVariable UUID contentId) { return content.setPublished(contentId, false); }

    @PostMapping("/content/{contentId}/move")
    public ContentDto move(@PathVariable UUID contentId, @RequestBody MoveContentRequest r) { return content.move(contentId, r); }

    @DeleteMapping("/content/{contentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID contentId) { content.delete(contentId); }

    @PostMapping("/content/{contentId}/done")
    public ContentProgressDto markDone(@PathVariable UUID contentId) { return content.markDone(contentId, true); }

    @DeleteMapping("/content/{contentId}/done")
    public ContentProgressDto undoDone(@PathVariable UUID contentId) { return content.markDone(contentId, false); }

    @GetMapping("/content/{contentId}/progress")
    public ContentProgressDto myProgress(@PathVariable UUID contentId) { return content.myProgress(contentId); }
}
