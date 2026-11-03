package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AddModuleRequirementRequest;
import learning_management_system.backend.dto.CourseModuleDto;
import learning_management_system.backend.dto.ModuleRequirementDto;
import learning_management_system.backend.service.ModuleCompletionRequirementService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** module_completion_requirement: a module's completion requirements. */
@RestController
@RequestMapping("/api/v1/modules/{moduleId}/requirements")
public class ModuleCompletionRequirementController {
    private final ModuleCompletionRequirementService requirements;

    public ModuleCompletionRequirementController(ModuleCompletionRequirementService requirements) { this.requirements = requirements; }

    @GetMapping
    public List<ModuleRequirementDto> list(@PathVariable UUID moduleId) { return requirements.list(moduleId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseModuleDto add(@PathVariable UUID moduleId, @Valid @RequestBody AddModuleRequirementRequest r) { return requirements.add(moduleId, r); }

    @DeleteMapping("/{requirementId}")
    public CourseModuleDto remove(@PathVariable UUID moduleId, @PathVariable UUID requirementId) { return requirements.remove(moduleId, requirementId); }
}
