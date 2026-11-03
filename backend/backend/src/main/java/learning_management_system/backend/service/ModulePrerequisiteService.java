package learning_management_system.backend.service;

import learning_management_system.backend.dto.CourseModuleDto;
import learning_management_system.backend.dto.ModulePrerequisiteDto;
import learning_management_system.backend.dto.ModulePrerequisitesRequest;

import java.util.List;
import java.util.UUID;

/** module_prerequisite: earlier modules a student must complete first. The course's teachers set them. */
public interface ModulePrerequisiteService {

    List<ModulePrerequisiteDto> list(UUID moduleId);

    /** Replaces the module's prerequisites; each must be a module that comes earlier in the course. */
    CourseModuleDto replace(UUID moduleId, ModulePrerequisitesRequest r);
}
