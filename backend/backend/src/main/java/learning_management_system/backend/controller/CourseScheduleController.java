package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CourseScheduleDto;
import learning_management_system.backend.dto.CourseScheduleRequest;
import learning_management_system.backend.dto.MyMeetingDto;
import learning_management_system.backend.service.CourseScheduleService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** course_schedule: a section's weekly meetings, and the caller's own timetable. */
@RestController
@RequestMapping("/api/v1")
public class CourseScheduleController {
    private final CourseScheduleService schedule;

    public CourseScheduleController(CourseScheduleService schedule) { this.schedule = schedule; }

    @GetMapping("/sections/{sectionId}/schedule")
    public List<CourseScheduleDto> list(@PathVariable UUID sectionId) { return schedule.list(sectionId); }

    @PostMapping("/sections/{sectionId}/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseScheduleDto create(@PathVariable UUID sectionId, @Valid @RequestBody CourseScheduleRequest r) { return schedule.create(sectionId, r); }

    @PutMapping("/sections/{sectionId}/schedule/{scheduleId}")
    public CourseScheduleDto replace(@PathVariable UUID sectionId, @PathVariable UUID scheduleId, @Valid @RequestBody CourseScheduleRequest r) {
        return schedule.replace(sectionId, scheduleId, r);
    }

    @DeleteMapping("/sections/{sectionId}/schedule/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID sectionId, @PathVariable UUID scheduleId) { schedule.delete(sectionId, scheduleId); }

    @GetMapping("/me/schedule")
    public List<MyMeetingDto> mine(@RequestParam(required = false) UUID termId) { return schedule.mySchedule(termId); }
}
