package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AddMasteryPathItemRequest;
import learning_management_system.backend.dto.MasteryPathItemDto;
import learning_management_system.backend.service.MasteryPathSetItemService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** mastery_path_set_item: the items a score range releases. */
@RestController
@RequestMapping("/api/v1")
public class MasteryPathSetItemController {
    private final MasteryPathSetItemService items;

    public MasteryPathSetItemController(MasteryPathSetItemService items) { this.items = items; }

    @PostMapping("/mastery-path-ranges/{rangeId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public MasteryPathItemDto add(@PathVariable UUID rangeId, @Valid @RequestBody AddMasteryPathItemRequest r) { return items.add(rangeId, r); }

    @DeleteMapping("/mastery-path-items/{setItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID setItemId) { items.delete(setItemId); }
}
