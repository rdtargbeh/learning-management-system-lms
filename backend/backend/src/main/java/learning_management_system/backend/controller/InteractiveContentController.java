package learning_management_system.backend.controller;

import learning_management_system.backend.dto.InteractiveContentDto;
import learning_management_system.backend.service.InteractiveContentService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** interactive_content: launch details of an interactive, SCORM or external-tool item. */
@RestController
@RequestMapping("/api/v1/content/{contentId}/interactive")
public class InteractiveContentController {
    private final InteractiveContentService interactive;

    public InteractiveContentController(InteractiveContentService interactive) { this.interactive = interactive; }

    @GetMapping
    public InteractiveContentDto get(@PathVariable UUID contentId) { return interactive.get(contentId); }

    @PutMapping
    public InteractiveContentDto replace(@PathVariable UUID contentId, @Valid @RequestBody InteractiveContentDto d) { return interactive.replace(contentId, d); }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID contentId) { interactive.delete(contentId); }
}
