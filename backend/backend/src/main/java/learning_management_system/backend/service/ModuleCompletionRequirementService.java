package learning_management_system.backend.service;

import learning_management_system.backend.dto.AddModuleRequirementRequest;
import learning_management_system.backend.dto.CourseModuleDto;
import learning_management_system.backend.dto.ModuleRequirementDto;

import java.util.List;
import java.util.UUID;

/** module_completion_requirement: what a student must do to complete a module. The course's teachers set them. */
public interface ModuleCompletionRequirementService {

    List<ModuleRequirementDto> list(UUID moduleId);

    /** Adds or replaces the requirement for one item of the module. */
    CourseModuleDto add(UUID moduleId, AddModuleRequirementRequest r);

    CourseModuleDto remove(UUID moduleId, UUID requirementId);
}
