package learning_management_system.backend.controller;

import learning_management_system.backend.dto.ContentBlockDto;
import learning_management_system.backend.dto.ContentBlockRequest;
import learning_management_system.backend.dto.ReorderRequest;
import learning_management_system.backend.service.ContentBlockService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** content_block: the blocks of a page. */
@RestController
@RequestMapping("/api/v1")
public class ContentBlockController {
    private final ContentBlockService blocks;

    public ContentBlockController(ContentBlockService blocks) { this.blocks = blocks; }

    @GetMapping("/content/{contentId}/blocks")
    public List<ContentBlockDto> list(@PathVariable UUID contentId) { return blocks.list(contentId); }

    @PostMapping("/content/{contentId}/blocks")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentBlockDto add(@PathVariable UUID contentId, @Valid @RequestBody ContentBlockRequest r) { return blocks.add(contentId, r); }

    @PutMapping("/content/{contentId}/blocks/order")
    public List<ContentBlockDto> reorder(@PathVariable UUID contentId, @Valid @RequestBody ReorderRequest r) { return blocks.reorder(contentId, r); }

    @PatchMapping("/content-blocks/{blockId}")
    public ContentBlockDto update(@PathVariable UUID blockId, @Valid @RequestBody ContentBlockRequest r) { return blocks.update(blockId, r); }

    @DeleteMapping("/content-blocks/{blockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID blockId) { blocks.delete(blockId); }
}
