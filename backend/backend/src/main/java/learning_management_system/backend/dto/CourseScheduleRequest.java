package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import learning_management_system.backend.enums.DayOfWeek;
import learning_management_system.backend.enums.MeetingType;

import java.time.LocalTime;
import java.util.UUID;

/**
 * A weekly meeting (POST creates, PUT replaces). IN_PERSON, HYBRID, ONLINE and SYNCHRONOUS meetings need a day and
 * times; IN_PERSON and HYBRID also need a location. ASYNCHRONOUS has no day or time. instructorAssignmentId is a
 * staff assignment of the same section.
 */
public record CourseScheduleRequest(@NotNull MeetingType meetingType, DayOfWeek dayOfWeek, LocalTime timeStart, LocalTime timeEnd,
                                    @Size(max = 255) String location, UUID instructorAssignmentId) {}
