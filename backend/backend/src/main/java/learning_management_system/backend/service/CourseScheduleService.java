package learning_management_system.backend.service;

import learning_management_system.backend.dto.CourseScheduleDto;
import learning_management_system.backend.dto.CourseScheduleRequest;
import learning_management_system.backend.dto.MyMeetingDto;

import java.util.List;
import java.util.UUID;

/**
 * course_schedule: when and where a section meets each week. Course admins (COURSE_MANAGE) set meetings; a room or
 * an instructor cannot be booked twice at the same time in the same term (409 ROOM_CONFLICT / INSTRUCTOR_CONFLICT).
 */
public interface CourseScheduleService {

    List<CourseScheduleDto> list(UUID sectionId);

    CourseScheduleDto create(UUID sectionId, CourseScheduleRequest r);

    CourseScheduleDto replace(UUID sectionId, UUID scheduleId, CourseScheduleRequest r);

    void delete(UUID sectionId, UUID scheduleId);

    /** The caller's weekly timetable from active enrollments and staff assignments; termId narrows it to one term. */
    List<MyMeetingDto> mySchedule(UUID termId);
}
