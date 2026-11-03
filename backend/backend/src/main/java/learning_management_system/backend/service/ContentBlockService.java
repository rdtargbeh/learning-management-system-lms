package learning_management_system.backend.service;

import learning_management_system.backend.dto.ContentBlockDto;
import learning_management_system.backend.dto.ContentBlockRequest;
import learning_management_system.backend.dto.ReorderRequest;

import java.util.List;
import java.util.UUID;

/**
 * content_block: a page built from blocks. Anyone who may open the page reads its blocks (students get 409
 * CONTENT_LOCKED while the page is locked for them); the course's teachers edit them.
 */
public interface ContentBlockService {

    List<ContentBlockDto> list(UUID contentId);

    ContentBlockDto add(UUID contentId, ContentBlockRequest r);

    ContentBlockDto update(UUID blockId, ContentBlockRequest r);

    void delete(UUID blockId);

    /** Must list every block of the page. */
    List<ContentBlockDto> reorder(UUID contentId, ReorderRequest r);
}
