package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CoursePaceItemDto;
import learning_management_system.backend.dto.UpdatePaceItemRequest;
import learning_management_system.backend.service.CoursePaceItemService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** course_pace_item: the steps of a pace. */
@RestController
@RequestMapping("/api/v1")
public class CoursePaceItemController {
    private final CoursePaceItemService items;

    public CoursePaceItemController(CoursePaceItemService items) { this.items = items; }

    @GetMapping("/paces/{paceId}/items")
    public List<CoursePaceItemDto> list(@PathVariable UUID paceId) { return items.list(paceId); }

    @PatchMapping("/pace-items/{paceItemId}")
    public CoursePaceItemDto update(@PathVariable UUID paceItemId, @Valid @RequestBody UpdatePaceItemRequest r) { return items.update(paceItemId, r); }
}
