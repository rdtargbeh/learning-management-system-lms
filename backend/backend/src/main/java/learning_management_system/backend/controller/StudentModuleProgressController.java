package learning_management_system.backend.controller;

import learning_management_system.backend.dto.ModuleStudentProgressDto;
import learning_management_system.backend.service.StudentModuleProgressService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** student_module_progress: every student's progress in a module, for course staff. */
@RestController
@RequestMapping("/api/v1/modules/{moduleId}/progress")
public class StudentModuleProgressController {
    private final StudentModuleProgressService progress;

    public StudentModuleProgressController(StudentModuleProgressService progress) { this.progress = progress; }

    @GetMapping
    public List<ModuleStudentProgressDto> list(@PathVariable UUID moduleId) { return progress.moduleProgress(moduleId); }
}
