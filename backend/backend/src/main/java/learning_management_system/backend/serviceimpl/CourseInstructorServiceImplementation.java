package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AssignCourseInstructorRequest;
import learning_management_system.backend.dto.CourseInstructorDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseInstructor;
import learning_management_system.backend.entity.CourseSection;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.CourseInstructorRole;
import learning_management_system.backend.enums.CourseStatus;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.CourseSectionMapper;
import learning_management_system.backend.repository.CourseInstructorRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.CourseSectionRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.CourseInstructorService;
import learning_management_system.backend.service.UserService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** course_instructor: section staff (PRIMARY, CO_INSTRUCTOR, TEACHING_ASSISTANT, GRADER, REVIEWER). */
@Service
public class CourseInstructorServiceImplementation implements CourseInstructorService {
    private final CourseInstructorRepository staff;
    private final CourseRepository courses;
    private final CourseSectionRepository sections;
    private final EnrollmentRepository enrollments;
    private final UserRepository users;
    private final UserService userService;
    private final CourseAccess access;
    private final CourseSectionMapper sectionMapper;
    private final EntityRefresher refresher;

    public CourseInstructorServiceImplementation(CourseInstructorRepository staff, CourseRepository courses, CourseSectionRepository sections,
                                                 EnrollmentRepository enrollments, UserRepository users, UserService userService,
                                                 CourseAccess access, CourseSectionMapper sectionMapper, EntityRefresher refresher) {
        this.staff = staff;
        this.courses = courses;
        this.sections = sections;
        this.enrollments = enrollments;
        this.users = users;
        this.userService = userService;
        this.access = access;
        this.sectionMapper = sectionMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseInstructorDto> list(UUID sectionId) {
        CourseSection s = section(sectionId);
        access.requireCourseViewer(course(s.getCourseId()));      // students see who teaches them, like Canvas "People"
        List<CourseInstructor> list = staff.findBySectionIdOrderByRoleAsc(sectionId);
        Map<UUID, UserSummaryDto> names = userService.summariesById(list.stream().map(CourseInstructor::getUserId).toList());
        return list.stream().map(ci -> toDto(ci, names.get(ci.getUserId()))).toList();
    }

    @Transactional
    @Override
    public CourseInstructorDto assign(UUID sectionId, AssignCourseInstructorRequest r) {
        access.requireCourseAdmin();
        CourseSection s = section(sectionId);
        requireOpen(course(s.getCourseId()));
        User u = users.findById(r.userId()).filter(x -> !Boolean.TRUE.equals(x.getIsSystemUser()))
                .orElseThrow(() -> ApiException.notFound("User"));
        if (u.getUserType() == UserType.OBSERVER) throw ApiException.badRequest("OBSERVER_CANNOT_TEACH", "Observers cannot be course staff");
        if (u.getUserType() == UserType.STUDENT
                && !EnumSet.of(CourseInstructorRole.TEACHING_ASSISTANT, CourseInstructorRole.GRADER).contains(r.role())) {
            throw ApiException.badRequest("STUDENT_ROLE_LIMIT", "A student account can only be a TEACHING_ASSISTANT or GRADER");
        }
        if (enrollments.findBySectionIdAndUserId(sectionId, r.userId())
                .filter(e -> e.getEnrollmentStatus() == EnrollmentStatus.ACTIVE
                        && EnrollmentProvisioningServiceImplementation.SEAT_TYPES.contains(e.getEnrollmentType())).isPresent()) {
            throw ApiException.conflict("USER_IS_ENROLLED", "The user is enrolled as a student in this section");
        }
        if (staff.findBySectionIdAndUserId(sectionId, r.userId()).isPresent()) {
            throw ApiException.conflict("ALREADY_STAFF", "The user is already on this section's staff");
        }
        if (r.dateStart() != null && r.dateEnd() != null && r.dateEnd().isBefore(r.dateStart())) {
            throw ApiException.badRequest("INVALID_DATES", "dateEnd must be on or after dateStart");
        }
        CourseInstructor ci = new CourseInstructor();
        ci.setSectionId(sectionId);
        ci.setUserId(r.userId());
        ci.setRole(r.role());
        ci.setIsPrimaryInstructor(r.role() == CourseInstructorRole.PRIMARY);   // one per section (unique index)
        ci.setDateAssigned(LocalDate.now());
        ci.setDateStart(r.dateStart());
        ci.setDateEnd(r.dateEnd());
        refresher.refresh(staff.saveAndFlush(ci));
        return toDto(ci, userService.summariesById(List.of(r.userId())).get(r.userId()));
    }

    @Transactional
    @Override
    public void remove(UUID sectionId, UUID courseInstructorId) {
        access.requireCourseAdmin();
        CourseInstructor ci = staff.findById(courseInstructorId).filter(x -> x.getSectionId().equals(sectionId))
                .orElseThrow(() -> ApiException.notFound("Staff assignment"));
        staff.delete(ci);
    }

    private CourseInstructorDto toDto(CourseInstructor ci, UserSummaryDto user) {
        return sectionMapper.toDto(ci, user == null ? null : user.username(), user == null ? null : user.displayName());
    }

    private static void requireOpen(Course c) {
        if (Boolean.TRUE.equals(c.getIsArchived()) || c.getCourseStatus() == CourseStatus.ARCHIVED) {
            throw ApiException.conflict("COURSE_ARCHIVED", "The course is archived");
        }
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }

    private CourseSection section(UUID id) {
        return sections.findById(id).orElseThrow(() -> ApiException.notFound("Section"));
    }
}
