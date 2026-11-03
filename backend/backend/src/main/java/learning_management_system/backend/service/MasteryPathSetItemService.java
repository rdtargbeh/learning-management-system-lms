package learning_management_system.backend.service;

import learning_management_system.backend.dto.AddMasteryPathItemRequest;
import learning_management_system.backend.dto.MasteryPathItemDto;

import java.util.UUID;

/** mastery_path_set_item: the content items and assignments a score range releases. */
public interface MasteryPathSetItemService {

    MasteryPathItemDto add(UUID rangeId, AddMasteryPathItemRequest r);

    void delete(UUID setItemId);
}
