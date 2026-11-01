package learning_management_system.backend.service;

import learning_management_system.backend.dto.ContentTypeDto;
import learning_management_system.backend.dto.CreateContentTypeRequest;
import learning_management_system.backend.entity.ContentType;

import java.util.List;

/**
 * Content types. The standard ones (PAGE, VIDEO, FILE, LINK, DOCUMENT, AUDIO, INTERACTIVE, EXTERNAL_TOOL, SCORM) are
 * created for an institution the first time they are needed; institutions can add their own.
 */
public interface ContentTypeService {

    List<ContentTypeDto> list();

    ContentTypeDto create(CreateContentTypeRequest r);

    /** The type with this code, creating a standard type on first use; 400 for an unknown code. */
    ContentType resolve(String typeCode);
}
