package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AccommodationDto;
import learning_management_system.backend.dto.CourseAccommodationDto;
import learning_management_system.backend.dto.CreateAccommodationRequest;
import learning_management_system.backend.dto.UpdateAccommodationRequest;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.Accommodation;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.AccommodationType;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.IdentityMapper;
import learning_management_system.backend.repository.AccommodationRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.AccommodationService;
import learning_management_system.backend.service.AuditService;
import learning_management_system.backend.service.UserService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccommodationServiceImplementation implements AccommodationService {
    private final AccommodationRepository accommodations;
    private final UserRepository users;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final UserService userService;
    private final AuditService audit;
    private final CourseAccess access;
    private final IdentityMapper identityMapper;
    private final EntityRefresher refresher;

    public AccommodationServiceImplementation(AccommodationRepository accommodations, UserRepository users, CourseRepository courses,
                                              EnrollmentRepository enrollments, UserService userService, AuditService audit,
                                              CourseAccess access, IdentityMapper identityMapper, EntityRefresher refresher) {
        this.accommodations = accommodations;
        this.users = users;
        this.courses = courses;
        this.enrollments = enrollments;
        this.userService = userService;
        this.audit = audit;
        this.access = access;
        this.identityMapper = identityMapper;
        this.refresher = refresher;
    }

    @Transactional
    @Override
    public AccommodationDto create(UUID userId, CreateAccommodationRequest r) {
        Access.requireTenantPermission(Permissions.ACCOMMODATION_MANAGE);
        User u = users.findById(userId).filter(x -> !Boolean.TRUE.equals(x.getIsSystemUser()))
                .orElseThrow(() -> ApiException.notFound("User"));
        if (u.getUserType() != UserType.STUDENT) throw ApiException.badRequest("NOT_A_STUDENT", "Accommodations are for students");
        if (r.courseId() != null) courses.findById(r.courseId()).orElseThrow(() -> ApiException.badRequest("UNKNOWN_COURSE", "Unknown course"));
        if (r.accommodationType() == AccommodationType.EXTENDED_TIME && r.extendedTimePercentage() == null) {
            throw ApiException.badRequest("PERCENTAGE_REQUIRED", "EXTENDED_TIME needs extendedTimePercentage (e.g. 150 = time and a half)");
        }
        validateDates(r.dateStart(), r.dateEnd());
        Accommodation a = identityMapper.toEntity(r);
        a.setUserId(userId);
        a.setApprovedBy(Access.ctx().userId());
        a.setDateApproved(OffsetDateTime.now());
        a.setIsActive(true);
        if (a.getIsConfidential() == null) a.setIsConfidential(false);
        refresher.refresh(accommodations.saveAndFlush(a));
        audit.record("ACCOMMODATION_APPROVED", "ACCOMMODATION", a.getAccommodationId(), userId, Severity.INFO, null,
                Map.of("type", a.getAccommodationType().name()));
        return identityMapper.toDto(a);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AccommodationDto> forUser(UUID userId) {
        if (!Access.isSelf(userId)) Access.requireTenantPermission(Permissions.ACCOMMODATION_MANAGE);
        return accommodations.findByUserIdOrderByDateCreatedDesc(userId).stream().map(identityMapper::toDto).toList();
    }

    @Transactional
    @Override
    public AccommodationDto update(UUID accommodationId, UpdateAccommodationRequest r) {
        Access.requireTenantPermission(Permissions.ACCOMMODATION_MANAGE);
        Accommodation a = accommodations.findById(accommodationId).orElseThrow(() -> ApiException.notFound("Accommodation"));
        identityMapper.updateEntity(r, a);
        validateDates(a.getDateStart(), a.getDateEnd());
        accommodations.saveAndFlush(a);
        audit.record("ACCOMMODATION_CHANGED", "ACCOMMODATION", accommodationId, a.getUserId(), Severity.INFO, null, null);
        return identityMapper.toDto(a);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseAccommodationDto> forCourse(UUID courseId) {
        Course c = courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course"));
        Access.requireTenant();
        boolean manager = Access.hasTenantPermission(Permissions.ACCOMMODATION_MANAGE);
        if (!manager && !access.isCourseStaff(courseId) && !access.canSeeEverything()) {
            throw ApiException.forbidden("Only course staff can see accommodations");
        }
        Set<UUID> students = enrollments.activeInCourseOfTypes(courseId, EnrollmentProvisioningServiceImplementation.SEAT_TYPES).stream()
                .map(Enrollment::getUserId).collect(Collectors.toSet());
        if (students.isEmpty()) return List.of();
        LocalDate today = LocalDate.now();
        List<Accommodation> list = accommodations.findByUserIdInAndIsActiveTrue(students).stream()
                .filter(a -> a.getCourseId() == null || a.getCourseId().equals(c.getCourseId()))
                .filter(a -> (a.getDateStart() == null || !today.isBefore(a.getDateStart())) && (a.getDateEnd() == null || !today.isAfter(a.getDateEnd())))
                .toList();
        Map<UUID, UserSummaryDto> names = userService.summariesById(list.stream().map(Accommodation::getUserId).collect(Collectors.toSet()));
        return list.stream().map(a -> {
            UserSummaryDto n = names.get(a.getUserId());
            boolean hide = Boolean.TRUE.equals(a.getIsConfidential()) && !manager;
            return new CourseAccommodationDto(a.getAccommodationId(), a.getUserId(), n == null ? null : n.username(),
                    n == null ? null : n.displayName(), a.getAccommodationType(), a.getExtendedTimePercentage(),
                    hide ? null : a.getDetails(), a.getDateStart(), a.getDateEnd());
        }).toList();
    }

    private static void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) throw ApiException.badRequest("INVALID_DATES", "dateEnd must be on or after dateStart");
    }
}
