package learning_management_system.backend.dto;

import learning_management_system.backend.enums.DayOfWeek;
import learning_management_system.backend.enums.MeetingType;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/** A weekly meeting of a section (table course_schedule), with the instructor who leads it when one is named. */
public record CourseScheduleDto(UUID scheduleId, UUID sectionId, UUID instructorAssignmentId, UUID instructorUserId, String instructorName,
                                DayOfWeek dayOfWeek, LocalTime timeStart, LocalTime timeEnd, String location, MeetingType meetingType,
                                OffsetDateTime dateCreated) {}
