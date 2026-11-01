package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CreateEnrollmentRequest;
import learning_management_system.backend.dto.EnrollmentDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseSection;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.enums.CourseSectionStatus;
import learning_management_system.backend.enums.CourseStatus;
import learning_management_system.backend.enums.EnrollmentSource;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.EnrollmentType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.EnrollmentMapper;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.CourseSectionRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.EnrollmentProvisioningService;
import learning_management_system.backend.service.EnrollmentService;
import learning_management_system.backend.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImplementation implements EnrollmentService {
    private final CourseRepository courses;
    private final CourseSectionRepository sections;
    private final EnrollmentRepository enrollments;
    private final EnrollmentProvisioningService provisioning;
    private final UserService userService;
    private final CourseAccess access;
    private final EnrollmentMapper enrollmentMapper;

    public EnrollmentServiceImplementation(CourseRepository courses, CourseSectionRepository sections,
                                           EnrollmentRepository enrollments, EnrollmentProvisioningService provisioning,
                                           UserService userService, CourseAccess access, EnrollmentMapper enrollmentMapper) {
        this.courses = courses;
        this.sections = sections;
        this.enrollments = enrollments;
        this.provisioning = provisioning;
        this.userService = userService;
        this.access = access;
        this.enrollmentMapper = enrollmentMapper;
    }

    @Transactional
    @Override
    public EnrollmentDto enroll(UUID sectionId, CreateEnrollmentRequest r) {
        Access.requireTenant();
        CourseSection s = provisioning.lockSection(sectionId);
        access.requireRosterManager(s);
        Course c = course(s.getCourseId());
        EnrollmentSource source = r.sisSourceId() != null ? EnrollmentSource.SIS : EnrollmentSource.MANUAL;
        Enrollment e = provisioning.enroll(s, c, r.userId(), r.enrollmentType(), r.observedUserId(), source,
                r.limitToSection(), r.sisSourceId());
        return toDto(e, c.getCourseId());
    }

    @Transactional
    @Override
    public EnrollmentDto selfEnroll(String code) {
        Access.requireTenant();
        UUID me = Access.ctx().userId();
        Course c = courses.findBySelfEnrollmentCode(code.trim().toUpperCase(Locale.ROOT))
                .filter(x -> Boolean.TRUE.equals(x.getAllowSelfEnrollment()) && x.getCourseStatus() == CourseStatus.PUBLISHED)
                .orElseThrow(() -> ApiException.notFound("Course for this code"));
        if (enrollments.isActiveInCourse(c.getCourseId(), me)) {
            throw ApiException.conflict("ALREADY_ENROLLED", "You are already enrolled in this course");
        }
        LocalDate today = LocalDate.now();
        boolean sawOpenSection = false;
        for (CourseSection candidate : sections.findByCourseIdOrderBySectionNumberAsc(c.getCourseId())) {
            if (candidate.getStatus() != CourseSectionStatus.ACTIVE || Boolean.FALSE.equals(candidate.getIsOpenForEnrollment())) continue;
            if (candidate.getRegistrationOpenDate() != null && today.isBefore(candidate.getRegistrationOpenDate())) continue;
            if (candidate.getRegistrationCloseDate() != null && today.isAfter(candidate.getRegistrationCloseDate())) continue;
            sawOpenSection = true;
            CourseSection s = provisioning.lockSection(candidate.getSectionId());
            Integer free = provisioning.openSeats(s);
            if (free != null && free <= 0) continue;
            return toDto(provisioning.enroll(s, c, me, EnrollmentType.STUDENT, null, EnrollmentSource.SELF, null, null), c.getCourseId());
        }
        if (!sawOpenSection) throw ApiException.conflict("ENROLLMENT_CLOSED", "No section of this course is open for enrollment");
        throw ApiException.conflict("SECTION_FULL", "Every open section is full; join a section's waitlist instead");
    }

    @Transactional(readOnly = true)
    @Override
    public List<EnrollmentDto> roster(UUID sectionId, EnrollmentStatus status, EnrollmentType type) {
        CourseSection s = section(sectionId);
        access.requireRosterViewer(s);
        return toDtos(enrollments.roster(sectionId, status, type));
    }

    @Transactional(readOnly = true)
    @Override
    public List<EnrollmentDto> forUser(UUID userId) {
        Access.requireTenant();
        if (!Access.isSelf(userId) && !Access.hasTenantPermission(Permissions.ENROLLMENT_MANAGE, Permissions.COURSE_READ,
                Permissions.USER_READ)) {
            throw ApiException.forbidden("You can only see your own enrollments");
        }
        return toDtos(enrollments.findByUserIdOrderByDateEnrolledDesc(userId));
    }

    @Transactional(readOnly = true)
    @Override
    public EnrollmentDto get(UUID enrollmentId) {
        Enrollment e = load(enrollmentId);
        CourseSection s = section(e.getSectionId());
        if (!Access.isSelf(e.getUserId())) access.requireRosterViewer(s);
        return toDto(e, s.getCourseId());
    }

    @Transactional
    @Override
    public EnrollmentDto changeStatus(UUID enrollmentId, EnrollmentStatus status) {
        Access.requireTenant();
        Enrollment e = load(enrollmentId);
        CourseSection s = provisioning.lockSection(e.getSectionId());
        boolean ownSelfDrop = Access.isSelf(e.getUserId()) && status == EnrollmentStatus.DROPPED
                && e.getEnrollmentSource() == EnrollmentSource.SELF;
        if (!ownSelfDrop) access.requireRosterManager(s);
        Course c = course(s.getCourseId());
        return toDto(provisioning.changeStatus(s, c, e, status), c.getCourseId());
    }

    // ---------------------------------------------------------------- helpers

    private List<EnrollmentDto> toDtos(List<Enrollment> list) {
        Map<UUID, UserSummaryDto> names = userService.summariesById(list.stream().map(Enrollment::getUserId).toList());
        Map<UUID, UUID> courseOf = sections.findAllById(list.stream().map(Enrollment::getSectionId).distinct().toList()).stream()
                .collect(Collectors.toMap(CourseSection::getSectionId, CourseSection::getCourseId));
        return list.stream().map(e -> {
            UserSummaryDto u = names.get(e.getUserId());
            return enrollmentMapper.toDto(e, courseOf.get(e.getSectionId()), u == null ? null : u.username(), u == null ? null : u.displayName());
        }).toList();
    }

    private EnrollmentDto toDto(Enrollment e, UUID courseId) {
        UserSummaryDto u = userService.summariesById(List.of(e.getUserId())).get(e.getUserId());
        return enrollmentMapper.toDto(e, courseId, u == null ? null : u.username(), u == null ? null : u.displayName());
    }

    private Enrollment load(UUID id) {
        return enrollments.findById(id).orElseThrow(() -> ApiException.notFound("Enrollment"));
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }

    private CourseSection section(UUID id) {
        return sections.findById(id).orElseThrow(() -> ApiException.notFound("Section"));
    }
}
