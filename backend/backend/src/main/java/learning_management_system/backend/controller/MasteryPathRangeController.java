package learning_management_system.backend.controller;

import learning_management_system.backend.dto.MasteryPathRangeDto;
import learning_management_system.backend.dto.MasteryPathRangeRequest;
import learning_management_system.backend.service.MasteryPathRangeService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** mastery_path_range: the score ranges of a mastery path. */
@RestController
@RequestMapping("/api/v1")
public class MasteryPathRangeController {
    private final MasteryPathRangeService ranges;

    public MasteryPathRangeController(MasteryPathRangeService ranges) { this.ranges = ranges; }

    @PostMapping("/mastery-paths/{ruleId}/ranges")
    @ResponseStatus(HttpStatus.CREATED)
    public MasteryPathRangeDto add(@PathVariable UUID ruleId, @Valid @RequestBody MasteryPathRangeRequest r) { return ranges.add(ruleId, r); }

    @PatchMapping("/mastery-path-ranges/{rangeId}")
    public MasteryPathRangeDto update(@PathVariable UUID rangeId, @Valid @RequestBody MasteryPathRangeRequest r) { return ranges.update(rangeId, r); }

    @DeleteMapping("/mastery-path-ranges/{rangeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID rangeId) { ranges.delete(rangeId); }
}
