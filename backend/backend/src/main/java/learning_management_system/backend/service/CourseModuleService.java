package learning_management_system.backend.service;

import learning_management_system.backend.dto.AddModuleRequirementRequest;
import learning_management_system.backend.dto.CourseModuleDto;
import learning_management_system.backend.dto.CreateCourseModuleRequest;
import learning_management_system.backend.dto.ModulePrerequisitesRequest;
import learning_management_system.backend.dto.ModuleStudentProgressDto;
import learning_management_system.backend.dto.ModuleViewDto;
import learning_management_system.backend.dto.ReorderRequest;
import learning_management_system.backend.dto.UpdateCourseModuleRequest;

import java.util.List;
import java.util.UUID;

/**
 * Course modules (Canvas Modules / Blackboard learning modules). Teachers build, order and publish modules, set
 * prerequisites and completion requirements; students see published modules with their own progress and locks.
 */
public interface CourseModuleService {

    /** Staff see every module and item; students see published ones with their progress. */
    List<ModuleViewDto> list(UUID courseId);

    ModuleViewDto get(UUID moduleId);

    CourseModuleDto create(UUID courseId, CreateCourseModuleRequest r);

    CourseModuleDto update(UUID moduleId, UpdateCourseModuleRequest r);

    /** Publishing can also publish every item in the module. */
    CourseModuleDto setPublished(UUID moduleId, boolean published, boolean includeItems);

    /** Deletes the module; its items stay in the course, outside any module. */
    void delete(UUID moduleId);

    List<ModuleViewDto> reorder(UUID courseId, ReorderRequest r);

    CourseModuleDto setPrerequisites(UUID moduleId, ModulePrerequisitesRequest r);

    CourseModuleDto addRequirement(UUID moduleId, AddModuleRequirementRequest r);

    CourseModuleDto removeRequirement(UUID moduleId, UUID requirementId);

    ModuleViewDto reorderItems(UUID moduleId, ReorderRequest r);

    List<ModuleStudentProgressDto> studentProgress(UUID moduleId);
}
