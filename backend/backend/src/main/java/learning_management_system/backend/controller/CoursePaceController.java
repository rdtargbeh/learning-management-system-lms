package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CoursePaceDto;
import learning_management_system.backend.dto.CoursePaceRequest;
import learning_management_system.backend.dto.PaceScheduleDto;
import learning_management_system.backend.service.CoursePaceService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** course_pace: a course's paces and paced schedules. */
@RestController
@RequestMapping("/api/v1")
public class CoursePaceController {
    private final CoursePaceService paces;

    public CoursePaceController(CoursePaceService paces) { this.paces = paces; }

    @GetMapping("/courses/{courseId}/paces")
    public List<CoursePaceDto> list(@PathVariable UUID courseId) { return paces.list(courseId); }

    @PostMapping("/courses/{courseId}/paces")
    @ResponseStatus(HttpStatus.CREATED)
    public CoursePaceDto create(@PathVariable UUID courseId, @RequestBody CoursePaceRequest r) { return paces.create(courseId, r); }

    @GetMapping("/courses/{courseId}/pace/mine")
    public PaceScheduleDto mine(@PathVariable UUID courseId) { return paces.mine(courseId); }

    @GetMapping("/paces/{paceId}")
    public CoursePaceDto get(@PathVariable UUID paceId) { return paces.get(paceId); }

    @PatchMapping("/paces/{paceId}")
    public CoursePaceDto update(@PathVariable UUID paceId, @RequestBody CoursePaceRequest r) { return paces.update(paceId, r); }

    @PostMapping("/paces/{paceId}/refresh")
    public CoursePaceDto refresh(@PathVariable UUID paceId) { return paces.refresh(paceId); }

    @PostMapping("/paces/{paceId}/publish")
    public CoursePaceDto publish(@PathVariable UUID paceId) { return paces.publish(paceId); }

    @PostMapping("/paces/{paceId}/archive")
    public CoursePaceDto archive(@PathVariable UUID paceId) { return paces.archive(paceId); }

    @DeleteMapping("/paces/{paceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID paceId) { paces.delete(paceId); }

    /** Projected dates for one student (userId) or from today. */
    @GetMapping("/paces/{paceId}/schedule")
    public PaceScheduleDto schedule(@PathVariable UUID paceId, @RequestParam(required = false) UUID userId) { return paces.schedule(paceId, userId); }
}
