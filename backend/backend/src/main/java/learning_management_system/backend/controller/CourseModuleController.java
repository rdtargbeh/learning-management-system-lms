package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AddModuleRequirementRequest;
import learning_management_system.backend.dto.CourseModuleDto;
import learning_management_system.backend.dto.CreateCourseModuleRequest;
import learning_management_system.backend.dto.ModulePrerequisitesRequest;
import learning_management_system.backend.dto.ModuleStudentProgressDto;
import learning_management_system.backend.dto.ModuleViewDto;
import learning_management_system.backend.dto.ReorderRequest;
import learning_management_system.backend.dto.UpdateCourseModuleRequest;
import learning_management_system.backend.service.CourseModuleService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Course modules, their order, prerequisites, requirements and student progress. */
@RestController
@RequestMapping("/api/v1")
public class CourseModuleController {
    private final CourseModuleService modules;

    public CourseModuleController(CourseModuleService modules) { this.modules = modules; }

    @GetMapping("/courses/{courseId}/modules")
    public List<ModuleViewDto> list(@PathVariable UUID courseId) { return modules.list(courseId); }

    @PostMapping("/courses/{courseId}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseModuleDto create(@PathVariable UUID courseId, @Valid @RequestBody CreateCourseModuleRequest r) {
        return modules.create(courseId, r);
    }

    @PutMapping("/courses/{courseId}/modules/order")
    public List<ModuleViewDto> reorder(@PathVariable UUID courseId, @Valid @RequestBody ReorderRequest r) {
        return modules.reorder(courseId, r);
    }

    @GetMapping("/modules/{moduleId}")
    public ModuleViewDto get(@PathVariable UUID moduleId) { return modules.get(moduleId); }

    @PatchMapping("/modules/{moduleId}")
    public CourseModuleDto update(@PathVariable UUID moduleId, @Valid @RequestBody UpdateCourseModuleRequest r) {
        return modules.update(moduleId, r);
    }

    /** includeItems=true also publishes every item in the module. */
    @PostMapping("/modules/{moduleId}/publish")
    public CourseModuleDto publish(@PathVariable UUID moduleId, @RequestParam(defaultValue = "false") boolean includeItems) {
        return modules.setPublished(moduleId, true, includeItems);
    }

    @PostMapping("/modules/{moduleId}/unpublish")
    public CourseModuleDto unpublish(@PathVariable UUID moduleId) { return modules.setPublished(moduleId, false, false); }

    @DeleteMapping("/modules/{moduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID moduleId) { modules.delete(moduleId); }

    @PutMapping("/modules/{moduleId}/prerequisites")
    public CourseModuleDto prerequisites(@PathVariable UUID moduleId, @Valid @RequestBody ModulePrerequisitesRequest r) {
        return modules.setPrerequisites(moduleId, r);
    }

    @PostMapping("/modules/{moduleId}/requirements")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseModuleDto addRequirement(@PathVariable UUID moduleId, @Valid @RequestBody AddModuleRequirementRequest r) {
        return modules.addRequirement(moduleId, r);
    }

    @DeleteMapping("/modules/{moduleId}/requirements/{requirementId}")
    public CourseModuleDto removeRequirement(@PathVariable UUID moduleId, @PathVariable UUID requirementId) {
        return modules.removeRequirement(moduleId, requirementId);
    }

    @PutMapping("/modules/{moduleId}/items/order")
    public ModuleViewDto reorderItems(@PathVariable UUID moduleId, @Valid @RequestBody ReorderRequest r) {
        return modules.reorderItems(moduleId, r);
    }

    /** Every student's progress in the module (course staff). */
    @GetMapping("/modules/{moduleId}/progress")
    public List<ModuleStudentProgressDto> progress(@PathVariable UUID moduleId) { return modules.studentProgress(moduleId); }
}
