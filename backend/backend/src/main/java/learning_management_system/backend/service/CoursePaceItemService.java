package learning_management_system.backend.service;

import learning_management_system.backend.dto.CoursePaceItemDto;
import learning_management_system.backend.dto.UpdatePaceItemRequest;

import java.util.List;
import java.util.UUID;

/** course_pace_item: the steps of a pace and their durations. */
public interface CoursePaceItemService {

    List<CoursePaceItemDto> list(UUID paceId);

    CoursePaceItemDto update(UUID paceItemId, UpdatePaceItemRequest r);
}
