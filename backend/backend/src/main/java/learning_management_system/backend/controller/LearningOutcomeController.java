package learning_management_system.backend.controller;

import learning_management_system.backend.dto.LearningOutcomeDto;
import learning_management_system.backend.dto.LearningOutcomeRequest;
import learning_management_system.backend.service.LearningOutcomeService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** learning_outcome: course outcomes, org-unit (program) outcomes, and single outcomes. */
@RestController
@RequestMapping("/api/v1")
public class LearningOutcomeController {
    private final LearningOutcomeService outcomes;

    public LearningOutcomeController(LearningOutcomeService outcomes) { this.outcomes = outcomes; }

    @GetMapping("/courses/{courseId}/outcomes")
    public List<LearningOutcomeDto> forCourse(@PathVariable UUID courseId, @RequestParam(defaultValue = "false") boolean includeInherited,
                                              @RequestParam(defaultValue = "false") boolean includeInactive) {
        return outcomes.forCourse(courseId, includeInherited, includeInactive);
    }

    @PostMapping("/courses/{courseId}/outcomes")
    @ResponseStatus(HttpStatus.CREATED)
    public LearningOutcomeDto createForCourse(@PathVariable UUID courseId, @Valid @RequestBody LearningOutcomeRequest r) {
        return outcomes.createForCourse(courseId, r);
    }

    @GetMapping("/org-units/{orgUnitId}/outcomes")
    public List<LearningOutcomeDto> forOrgUnit(@PathVariable UUID orgUnitId, @RequestParam(defaultValue = "false") boolean includeInactive) {
        return outcomes.forOrgUnit(orgUnitId, includeInactive);
    }

    @PostMapping("/org-units/{orgUnitId}/outcomes")
    @ResponseStatus(HttpStatus.CREATED)
    public LearningOutcomeDto createForOrgUnit(@PathVariable UUID orgUnitId, @Valid @RequestBody LearningOutcomeRequest r) {
        return outcomes.createForOrgUnit(orgUnitId, r);
    }

    @GetMapping("/outcomes/{outcomeId}")
    public LearningOutcomeDto get(@PathVariable UUID outcomeId) { return outcomes.get(outcomeId); }

    @PatchMapping("/outcomes/{outcomeId}")
    public LearningOutcomeDto update(@PathVariable UUID outcomeId, @Valid @RequestBody LearningOutcomeRequest r) { return outcomes.update(outcomeId, r); }

    @DeleteMapping("/outcomes/{outcomeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID outcomeId) { outcomes.delete(outcomeId); }
}
