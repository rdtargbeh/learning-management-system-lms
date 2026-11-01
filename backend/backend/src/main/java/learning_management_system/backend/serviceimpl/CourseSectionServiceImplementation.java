package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AssignCourseInstructorRequest;
import learning_management_system.backend.dto.CourseInstructorDto;
import learning_management_system.backend.dto.CourseSectionDto;
import learning_management_system.backend.dto.CreateCourseSectionRequest;
import learning_management_system.backend.dto.UpdateCourseSectionRequest;
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
import learning_management_system.backend.repository.AcademicTermRepository;
import learning_management_system.backend.repository.CourseInstructorRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.CourseSectionRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.CourseSectionService;
import learning_management_system.backend.service.EnrollmentProvisioningService;
import learning_management_system.backend.service.UserService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CourseSectionServiceImplementation implements CourseSectionService {
    private final CourseRepository courses;
    private final CourseSectionRepository sections;
    private final CourseInstructorRepository staff;
    private final EnrollmentRepository enrollments;
    private final AcademicTermRepository terms;
    private final UserRepository users;
    private final UserService userService;
    private final EnrollmentProvisioningService provisioning;
    private final CourseAccess access;
    private final CourseSectionMapper sectionMapper;
    private final EntityRefresher refresher;

    public CourseSectionServiceImplementation(CourseRepository courses, CourseSectionRepository sections,
                                              CourseInstructorRepository staff, EnrollmentRepository enrollments,
                                              AcademicTermRepository terms, UserRepository users, UserService userService,
                                              EnrollmentProvisioningService provisioning, CourseAccess access,
                                              CourseSectionMapper sectionMapper, EntityRefresher refresher) {
        this.courses = courses;
        this.sections = sections;
        this.staff = staff;
        this.enrollments = enrollments;
        this.terms = terms;
        this.users = users;
        this.userService = userService;
        this.provisioning = provisioning;
        this.access = access;
        this.sectionMapper = sectionMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseSectionDto> list(UUID courseId) {
        access.requireCourseViewer(course(courseId));
        return sections.findByCourseIdOrderBySectionNumberAsc(courseId).stream().map(sectionMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public CourseSectionDto get(UUID sectionId) {
        CourseSection s = section(sectionId);
        access.requireCourseViewer(course(s.getCourseId()));
        return sectionMapper.toDto(s);
    }

    @Transactional
    @Override
    public CourseSectionDto create(UUID courseId, CreateCourseSectionRequest r) {
        access.requireCourseAdmin();
        Course c = course(courseId);
        requireOpen(c);
        CourseSection s = sectionMapper.toEntity(r);
        s.setCourseId(courseId);
        s.setSectionNumber(r.sectionNumber().trim());
        s.setTermId(r.termId() != null ? term(r.termId()) : c.getTermId());
        validateDates(s);
        return sectionMapper.toDto(refresher.refresh(sections.saveAndFlush(s)));
    }

    @Transactional
    @Override
    public CourseSectionDto update(UUID sectionId, UpdateCourseSectionRequest r) {
        access.requireCourseAdmin();
        CourseSection s = provisioning.lockSection(sectionId);
        if (r.termId() != null) term(r.termId());
        if (r.capacity() != null && r.capacity() < s.getEnrolledCount()) {
            throw ApiException.conflict("CAPACITY_BELOW_ENROLLMENT",
                    "Capacity cannot be lower than the " + s.getEnrolledCount() + " students already enrolled");
        }
        sectionMapper.updateEntity(r, s);
        validateDates(s);
        sections.saveAndFlush(s);
        provisioning.fillOpenSeats(s, course(s.getCourseId()));     // more seats or reopened: move the waitlist
        return sectionMapper.toDto(s);
    }

    @Transactional
    @Override
    public CourseSectionDto crosslist(UUID sectionId, UUID targetCourseId) {
        access.requireCourseAdmin();
        CourseSection s = provisioning.lockSection(sectionId);
        Course target = course(targetCourseId);
        requireOpen(target);
        if (s.getCourseId().equals(targetCourseId)) throw ApiException.badRequest("SAME_COURSE", "The section is already in that course");
        if (s.getCrosslistedFromCourseId() == null) s.setCrosslistedFromCourseId(s.getCourseId());
        else if (s.getCrosslistedFromCourseId().equals(targetCourseId)) s.setCrosslistedFromCourseId(null);   // moving back home
        s.setCourseId(targetCourseId);
        return sectionMapper.toDto(sections.saveAndFlush(s));
    }

    @Transactional
    @Override
    public CourseSectionDto uncrosslist(UUID sectionId) {
        access.requireCourseAdmin();
        CourseSection s = provisioning.lockSection(sectionId);
        if (s.getCrosslistedFromCourseId() == null) throw ApiException.badRequest("NOT_CROSSLISTED", "The section is not cross-listed");
        course(s.getCrosslistedFromCourseId());
        s.setCourseId(s.getCrosslistedFromCourseId());
        s.setCrosslistedFromCourseId(null);
        return sectionMapper.toDto(sections.saveAndFlush(s));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseInstructorDto> staff(UUID sectionId) {
        CourseSection s = section(sectionId);
        access.requireCourseViewer(course(s.getCourseId()));      // students see who teaches them, like Canvas "People"
        List<CourseInstructor> list = staff.findBySectionIdOrderByRoleAsc(sectionId);
        Map<UUID, UserSummaryDto> names = userService.summariesById(list.stream().map(CourseInstructor::getUserId).toList());
        return list.stream().map(ci -> toDto(ci, names.get(ci.getUserId()))).toList();
    }

    @Transactional
    @Override
    public CourseInstructorDto assignStaff(UUID sectionId, AssignCourseInstructorRequest r) {
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
    public void removeStaff(UUID sectionId, UUID courseInstructorId) {
        access.requireCourseAdmin();
        CourseInstructor ci = staff.findById(courseInstructorId).filter(x -> x.getSectionId().equals(sectionId))
                .orElseThrow(() -> ApiException.notFound("Staff assignment"));
        staff.delete(ci);
    }

    // ---------------------------------------------------------------- helpers

    private CourseInstructorDto toDto(CourseInstructor ci, UserSummaryDto user) {
        return sectionMapper.toDto(ci, user == null ? null : user.username(), user == null ? null : user.displayName());
    }

    private static void requireOpen(Course c) {
        if (Boolean.TRUE.equals(c.getIsArchived()) || c.getCourseStatus() == CourseStatus.ARCHIVED) {
            throw ApiException.conflict("COURSE_ARCHIVED", "The course is archived");
        }
    }

    private static void validateDates(CourseSection s) {
        if (s.getDateStart() != null && s.getDateEnd() != null && s.getDateEnd().isBefore(s.getDateStart())) {
            throw ApiException.badRequest("INVALID_DATES", "dateEnd must be on or after dateStart");
        }
        if (s.getRegistrationOpenDate() != null && s.getRegistrationCloseDate() != null
                && s.getRegistrationCloseDate().isBefore(s.getRegistrationOpenDate())) {
            throw ApiException.badRequest("INVALID_DATES", "registrationCloseDate must be on or after registrationOpenDate");
        }
    }

    private UUID term(UUID termId) {
        return terms.findById(termId).orElseThrow(() -> ApiException.badRequest("UNKNOWN_TERM", "Unknown term")).getTermId();
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }

    private CourseSection section(UUID id) {
        return sections.findById(id).orElseThrow(() -> ApiException.notFound("Section"));
    }
}
