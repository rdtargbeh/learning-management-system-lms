package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CreateReleaseConditionRequest;
import learning_management_system.backend.dto.ReleaseConditionDto;
import learning_management_system.backend.enums.TargetType;
import learning_management_system.backend.service.ReleaseConditionService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** release_condition: a course's conditional-release rules. */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/release-conditions")
public class ReleaseConditionController {
    private final ReleaseConditionService conditions;

    public ReleaseConditionController(ReleaseConditionService conditions) { this.conditions = conditions; }

    @GetMapping
    public List<ReleaseConditionDto> list(@PathVariable UUID courseId, @RequestParam(required = false) TargetType targetType,
                                          @RequestParam(required = false) UUID targetId) {
        return conditions.list(courseId, targetType, targetId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReleaseConditionDto create(@PathVariable UUID courseId, @Valid @RequestBody CreateReleaseConditionRequest r) { return conditions.create(courseId, r); }

    @DeleteMapping("/{conditionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID courseId, @PathVariable UUID conditionId) { conditions.delete(courseId, conditionId); }
}
