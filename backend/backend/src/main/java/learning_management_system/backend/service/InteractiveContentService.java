package learning_management_system.backend.service;

import learning_management_system.backend.dto.InteractiveContentDto;

import java.util.UUID;

/** interactive_content: launch details of INTERACTIVE, SCORM and EXTERNAL_TOOL items (H5P, simulations, LTI tools ...). */
public interface InteractiveContentService {

    InteractiveContentDto get(UUID contentId);

    /** Creates or replaces the details (course editors). */
    InteractiveContentDto replace(UUID contentId, InteractiveContentDto d);

    void delete(UUID contentId);
}
