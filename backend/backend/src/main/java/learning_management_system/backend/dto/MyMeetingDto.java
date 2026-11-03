package learning_management_system.backend.dto;

import learning_management_system.backend.enums.DayOfWeek;
import learning_management_system.backend.enums.MeetingType;

import java.time.LocalTime;
import java.util.UUID;

/** One line of the signed-in user's weekly timetable. role: the enrollment type or the staff role. */
public record MyMeetingDto(UUID courseId, String courseCode, String courseTitle, UUID sectionId, String sectionNumber, String role,
                           DayOfWeek dayOfWeek, LocalTime timeStart, LocalTime timeEnd, String location, MeetingType meetingType) {}
