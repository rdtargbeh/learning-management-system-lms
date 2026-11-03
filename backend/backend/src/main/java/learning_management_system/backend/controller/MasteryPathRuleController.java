package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CreateMasteryPathRuleRequest;
import learning_management_system.backend.dto.MasteryPathRuleDto;
import learning_management_system.backend.dto.MyMasteryPathDto;
import learning_management_system.backend.service.MasteryPathRuleService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** mastery_path_rule: a course's mastery paths. */
@RestController
@RequestMapping("/api/v1")
public class MasteryPathRuleController {
    private final MasteryPathRuleService rules;

    public MasteryPathRuleController(MasteryPathRuleService rules) { this.rules = rules; }

    @GetMapping("/courses/{courseId}/mastery-paths")
    public List<MasteryPathRuleDto> list(@PathVariable UUID courseId) { return rules.list(courseId); }

    @PostMapping("/courses/{courseId}/mastery-paths")
    @ResponseStatus(HttpStatus.CREATED)
    public MasteryPathRuleDto create(@PathVariable UUID courseId, @Valid @RequestBody CreateMasteryPathRuleRequest r) { return rules.create(courseId, r); }

    @GetMapping("/mastery-paths/{ruleId}")
    public MasteryPathRuleDto get(@PathVariable UUID ruleId) { return rules.get(ruleId); }

    @DeleteMapping("/mastery-paths/{ruleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID ruleId) { rules.delete(ruleId); }

    @GetMapping("/mastery-paths/{ruleId}/mine")
    public MyMasteryPathDto mine(@PathVariable UUID ruleId) { return rules.mine(ruleId); }
}
