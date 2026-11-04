package learning_management_system.backend.service;

import learning_management_system.backend.dto.ContentDto;
import learning_management_system.backend.dto.ContentProgressDto;
import learning_management_system.backend.dto.CreateContentRequest;
import learning_management_system.backend.dto.MoveContentRequest;
import learning_management_system.backend.dto.UpdateContentRequest;

import java.util.List;
import java.util.UUID;

/**
 * Content items of a course: pages, videos, files, links ... (Canvas Pages, Blackboard documents). Teachers create,
 * edit, publish and place items in modules; one page can be the course front page. Students open published items they
 * are allowed to see, which records their progress; locked items come back without a body and with a lockReason.
 * Pages whose editingRoles allow it can also be edited by students.
 */
public interface ContentService {

    /** Staff: every item, optionally of one type. Students: the published items they can see. */
    List<ContentDto> list(UUID courseId, String typeCode);

    ContentDto frontPage(UUID courseId);

    /** Opening an item as a student counts as a view. */
    ContentDto get(UUID contentId);

    ContentDto create(UUID courseId, CreateContentRequest r);

    ContentDto update(UUID contentId, UpdateContentRequest r);

    ContentDto setPublished(UUID contentId, boolean published);

    ContentDto move(UUID contentId, MoveContentRequest r);

    void delete(UUID contentId);

    /** "Mark as done" / undo, for students. */
    ContentProgressDto markDone(UUID contentId, boolean done);

    /** The caller's own progress on the item. */
    ContentProgressDto myProgress(UUID contentId);
}
