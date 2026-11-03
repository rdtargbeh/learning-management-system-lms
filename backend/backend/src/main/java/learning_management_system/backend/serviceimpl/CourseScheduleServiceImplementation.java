package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CourseScheduleDto;
import learning_management_system.backend.dto.CourseScheduleRequest;
import learning_management_system.backend.dto.MyMeetingDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseInstructor;
import learning_management_system.backend.entity.CourseSchedule;
import learning_management_system.backend.entity.CourseSection;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.enums.CourseSectionStatus;
import learning_management_system.backend.enums.CourseStatus;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.MeetingType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.CourseInstructorRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.CourseScheduleRepository;
import learning_management_system.backend.repository.CourseSectionRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.CourseScheduleService;
import learning_management_system.backend.service.UserService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CourseScheduleServiceImplementation implements CourseScheduleService {
    private static final Set<MeetingType> TIMED = EnumSet.of(MeetingType.IN_PERSON, MeetingType.HYBRID, MeetingType.ONLINE, MeetingType.SYNCHRONOUS);
    private static final Set<MeetingType> NEEDS_ROOM = EnumSet.of(MeetingType.IN_PERSON, MeetingType.HYBRID);

    private final CourseScheduleRepository meetings;
    private final CourseSectionRepository sections;
    private final CourseRepository courses;
    private final CourseInstructorRepository staff;
    private final EnrollmentRepository enrollments;
    private final UserService userService;
    private final CourseAccess access;
    private final EntityRefresher refresher;

    public CourseScheduleServiceImplementation(CourseScheduleRepository meetings, CourseSectionRepository sections, CourseRepository courses,
                                               CourseInstructorRepository staff, EnrollmentRepository enrollments, UserService userService,
                                               CourseAccess access, EntityRefresher refresher) {
        this.meetings = meetings;
        this.sections = sections;
        this.courses = courses;
        this.staff = staff;
        this.enrollments = enrollments;
        this.userService = userService;
        this.access = access;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseScheduleDto> list(UUID sectionId) {
        CourseSection s = section(sectionId);
        access.requireCourseViewer(course(s.getCourseId()));
        return toDtos(sorted(meetings.findBySectionId(sectionId)));
    }

    @Transactional
    @Override
    public CourseScheduleDto create(UUID sectionId, CourseScheduleRequest r) {
        access.requireCourseAdmin();
        CourseSection s = section(sectionId);
        CourseSchedule m = new CourseSchedule();
        m.setSectionId(sectionId);
        apply(m, r, s);
        refresher.refresh(meetings.saveAndFlush(m));
        return toDtos(List.of(m)).get(0);
    }

    @Transactional
    @Override
    public CourseScheduleDto replace(UUID sectionId, UUID scheduleId, CourseScheduleRequest r) {
        access.requireCourseAdmin();
        CourseSection s = section(sectionId);
        CourseSchedule m = meetings.findByScheduleIdAndSectionId(scheduleId, sectionId).orElseThrow(() -> ApiException.notFound("Meeting"));
        apply(m, r, s);
        meetings.saveAndFlush(m);
        return toDtos(List.of(m)).get(0);
    }

    @Transactional
    @Override
    public void delete(UUID sectionId, UUID scheduleId) {
        access.requireCourseAdmin();
        meetings.delete(meetings.findByScheduleIdAndSectionId(scheduleId, sectionId).orElseThrow(() -> ApiException.notFound("Meeting")));
    }

    @Transactional(readOnly = true)
    @Override
    public List<MyMeetingDto> mySchedule(UUID termId) {
        Access.requireTenant();
        UUID me = Access.ctx().userId();
        Map<UUID, String> roleBySection = new LinkedHashMap<>();
        for (Enrollment e : enrollments.findByUserIdOrderByDateEnrolledDesc(me)) {
            if (e.getEnrollmentStatus() == EnrollmentStatus.ACTIVE) roleBySection.putIfAbsent(e.getSectionId(), e.getEnrollmentType().name());
        }
        for (CourseInstructor ci : staff.findByUserId(me)) roleBySection.put(ci.getSectionId(), ci.getRole().name());
        if (roleBySection.isEmpty()) return List.of();
        Map<UUID, CourseSection> secs = sections.findAllById(roleBySection.keySet()).stream()
                .filter(s -> s.getStatus() == CourseSectionStatus.ACTIVE && (termId == null || termId.equals(s.getTermId())))
                .collect(Collectors.toMap(CourseSection::getSectionId, Function.identity()));
        Map<UUID, Course> crs = courses.findAllById(secs.values().stream().map(CourseSection::getCourseId).collect(Collectors.toSet())).stream()
                .filter(c -> c.getCourseStatus() != CourseStatus.ARCHIVED)
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));
        List<MyMeetingDto> out = new ArrayList<>();
        for (CourseSchedule m : sorted(meetings.findBySectionIdIn(secs.keySet()))) {
            CourseSection s = secs.get(m.getSectionId());
            Course c = crs.get(s.getCourseId());
            if (c == null) continue;
            String role = roleBySection.get(s.getSectionId());
            // students only see courses that are open to them
            if (!access.isSectionStaff(s.getSectionId()) && c.getCourseStatus() == CourseStatus.UNPUBLISHED) continue;
            out.add(new MyMeetingDto(c.getCourseId(), c.getCourseCode(), c.getCourseTitle(), s.getSectionId(), s.getSectionNumber(), role,
                    m.getDayOfWeek(), m.getTimeStart(), m.getTimeEnd(), m.getLocation(), m.getMeetingType()));
        }
        return out;
    }

    // ---------------------------------------------------------------- rules

    private void apply(CourseSchedule m, CourseScheduleRequest r, CourseSection s) {
        boolean timed = TIMED.contains(r.meetingType());
        if (timed && (r.dayOfWeek() == null || r.timeStart() == null || r.timeEnd() == null)) {
            throw ApiException.badRequest("TIME_REQUIRED", r.meetingType() + " meetings need dayOfWeek, timeStart and timeEnd");
        }
        if (!timed && (r.dayOfWeek() != null || r.timeStart() != null || r.timeEnd() != null)) {
            throw ApiException.badRequest("NO_TIME_FOR_ASYNC", "ASYNCHRONOUS meetings have no day or time");
        }
        if (timed && !r.timeEnd().isAfter(r.timeStart())) throw ApiException.badRequest("INVALID_TIMES", "timeEnd must be after timeStart");
        String location = r.location() == null || r.location().isBlank() ? null : r.location().trim();
        if (NEEDS_ROOM.contains(r.meetingType()) && location == null) {
            throw ApiException.badRequest("LOCATION_REQUIRED", r.meetingType() + " meetings need a location");
        }
        CourseInstructor lead = null;
        if (r.instructorAssignmentId() != null) {
            lead = staff.findById(r.instructorAssignmentId()).filter(ci -> ci.getSectionId().equals(s.getSectionId()))
                    .orElseThrow(() -> ApiException.badRequest("UNKNOWN_INSTRUCTOR", "instructorAssignmentId must be staff of this section"));
        }
        if (timed) conflicts(m.getScheduleId(), s, r, location, lead);
        m.setMeetingType(r.meetingType());
        m.setDayOfWeek(r.dayOfWeek());
        m.setTimeStart(r.timeStart());
        m.setTimeEnd(r.timeEnd());
        m.setLocation(location);
        m.setInstructorAssignmentId(r.instructorAssignmentId());
    }

    /** Same room, or same instructor, at an overlapping time in an active section of the same term. */
    private void conflicts(UUID self, CourseSection s, CourseScheduleRequest r, String location, CourseInstructor lead) {
        List<CourseSchedule> same = meetings.overlapping(r.dayOfWeek(), r.timeStart(), r.timeEnd()).stream()
                .filter(x -> !x.getScheduleId().equals(self)).toList();
        if (same.isEmpty()) return;
        Map<UUID, CourseSection> secs = sections.findAllById(same.stream().map(CourseSchedule::getSectionId).collect(Collectors.toSet())).stream()
                .filter(x -> x.getStatus() == CourseSectionStatus.ACTIVE && Objects.equals(x.getTermId(), s.getTermId()))
                .collect(Collectors.toMap(CourseSection::getSectionId, Function.identity()));
        Map<UUID, UUID> leadUser = new HashMap<>();
        for (CourseSchedule x : same) {
            CourseSection other = secs.get(x.getSectionId());
            if (other == null) continue;
            if (location != null && x.getLocation() != null && location.equalsIgnoreCase(x.getLocation())) {
                throw ApiException.conflict("ROOM_CONFLICT", location + " is already booked then by " + label(other));
            }
            if (lead != null && x.getInstructorAssignmentId() != null) {
                UUID u = leadUser.computeIfAbsent(x.getInstructorAssignmentId(),
                        id -> staff.findById(id).map(CourseInstructor::getUserId).orElse(null));
                if (lead.getUserId().equals(u)) {
                    throw ApiException.conflict("INSTRUCTOR_CONFLICT", "The instructor already teaches " + label(other) + " at that time");
                }
            }
        }
    }

    private String label(CourseSection s) {
        return courses.findById(s.getCourseId()).map(Course::getCourseCode).orElse("another course") + " section " + s.getSectionNumber();
    }

    // ---------------------------------------------------------------- views

    private static List<CourseSchedule> sorted(List<CourseSchedule> list) {
        return list.stream().sorted(Comparator
                .comparing((CourseSchedule m) -> m.getDayOfWeek() == null ? 99 : m.getDayOfWeek().ordinal())
                .thenComparing(m -> m.getTimeStart() == null ? java.time.LocalTime.MAX : m.getTimeStart())).toList();
    }

    private List<CourseScheduleDto> toDtos(List<CourseSchedule> list) {
        Map<UUID, CourseInstructor> leads = staff.findAllById(list.stream().map(CourseSchedule::getInstructorAssignmentId)
                .filter(Objects::nonNull).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(CourseInstructor::getCourseInstructorId, Function.identity()));
        Map<UUID, UserSummaryDto> names = userService.summariesById(leads.values().stream().map(CourseInstructor::getUserId).toList());
        return list.stream().map(m -> {
            CourseInstructor ci = m.getInstructorAssignmentId() == null ? null : leads.get(m.getInstructorAssignmentId());
            UserSummaryDto u = ci == null ? null : names.get(ci.getUserId());
            return new CourseScheduleDto(m.getScheduleId(), m.getSectionId(), m.getInstructorAssignmentId(), ci == null ? null : ci.getUserId(),
                    u == null ? null : u.displayName(), m.getDayOfWeek(), m.getTimeStart(), m.getTimeEnd(), m.getLocation(), m.getMeetingType(),
                    m.getDateCreated());
        }).toList();
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }

    private CourseSection section(UUID id) {
        return sections.findById(id).orElseThrow(() -> ApiException.notFound("Section"));
    }
}
