package learning_management_system.backend.service;

import learning_management_system.backend.dto.SystemResourceDto;
import learning_management_system.backend.dto.SystemResourceRequest;
import learning_management_system.backend.enums.SystemResourceLibraryResourceType;

import java.util.List;
import java.util.UUID;

/**
 * system_resource_library: shared templates, widgets, tools and assets. Platform admins publish platform-wide
 * resources; institution staff (instructors, TAs, staff, advisors, admins) suggest institution resources, which a
 * library manager (COURSE_MANAGE) approves. Users see approved resources (students only those marked public), plus
 * their own suggestions; managers see everything of their institution.
 */
public interface SystemResourceLibraryService {

    List<SystemResourceDto> list(SystemResourceLibraryResourceType type, String q);

    SystemResourceDto get(UUID resourceId);

    SystemResourceDto create(SystemResourceRequest r);

    /** The creator while it is unapproved, or a manager. */
    SystemResourceDto update(UUID resourceId, SystemResourceRequest r);

    SystemResourceDto approve(UUID resourceId, boolean approved);

    void delete(UUID resourceId);

    /** Counts a use (adding it to a course) and returns the resource. */
    SystemResourceDto use(UUID resourceId);

    /** Counts a download and returns the resource (with its URL). */
    SystemResourceDto download(UUID resourceId);
}
