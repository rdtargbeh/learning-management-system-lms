package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CourseModuleDto;
import learning_management_system.backend.dto.ModulePrerequisiteDto;
import learning_management_system.backend.dto.ModulePrerequisitesRequest;
import learning_management_system.backend.service.ModulePrerequisiteService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** module_prerequisite: a module's prerequisites. */
@RestController
@RequestMapping("/api/v1/modules/{moduleId}/prerequisites")
public class ModulePrerequisiteController {
    private final ModulePrerequisiteService prerequisites;

    public ModulePrerequisiteController(ModulePrerequisiteService prerequisites) { this.prerequisites = prerequisites; }

    @GetMapping
    public List<ModulePrerequisiteDto> list(@PathVariable UUID moduleId) { return prerequisites.list(moduleId); }

    @PutMapping
    public CourseModuleDto replace(@PathVariable UUID moduleId, @Valid @RequestBody ModulePrerequisitesRequest r) { return prerequisites.replace(moduleId, r); }
}
