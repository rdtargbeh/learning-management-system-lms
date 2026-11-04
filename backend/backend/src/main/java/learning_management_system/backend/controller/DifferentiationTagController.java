package learning_management_system.backend.controller;

import learning_management_system.backend.dto.DifferentiationTagDto;
import learning_management_system.backend.dto.DifferentiationTagRequest;
import learning_management_system.backend.service.DifferentiationTagService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** differentiation_tag: a course's staff-only student tags. */
@RestController
@RequestMapping("/api/v1")
public class DifferentiationTagController {
    private final DifferentiationTagService tags;

    public DifferentiationTagController(DifferentiationTagService tags) { this.tags = tags; }

    @GetMapping("/courses/{courseId}/tags")
    public List<DifferentiationTagDto> list(@PathVariable UUID courseId) { return tags.list(courseId); }

    @PostMapping("/courses/{courseId}/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public DifferentiationTagDto create(@PathVariable UUID courseId, @Valid @RequestBody DifferentiationTagRequest r) { return tags.create(courseId, r); }

    @PatchMapping("/tags/{tagId}")
    public DifferentiationTagDto update(@PathVariable UUID tagId, @Valid @RequestBody DifferentiationTagRequest r) { return tags.update(tagId, r); }

    @DeleteMapping("/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID tagId) { tags.delete(tagId); }
}
