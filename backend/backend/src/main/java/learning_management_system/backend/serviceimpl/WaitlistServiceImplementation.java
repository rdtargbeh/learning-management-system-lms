package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.EnrollmentDto;
import learning_management_system.backend.dto.JoinWaitlistRequest;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.dto.WaitlistDto;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseSection;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.entity.Waitlist;
import learning_management_system.backend.enums.CourseSectionStatus;
import learning_management_system.backend.enums.CourseStatus;
import learning_management_system.backend.enums.EnrollmentSource;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.EnrollmentType;
import learning_management_system.backend.enums.WaitlistStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.EnrollmentMapper;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.CourseSectionRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.repository.WaitlistRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.CoursePrerequisiteService;
import learning_management_system.backend.service.EnrollmentProvisioningService;
import learning_management_system.backend.service.UserService;
import learning_management_system.backend.service.WaitlistService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WaitlistServiceImplementation implements WaitlistService {
    private final WaitlistRepository waitlist;
    private final CourseRepository courses;
    private final CourseSectionRepository sections;
    private final EnrollmentRepository enrollments;
    private final UserRepository users;
    private final EnrollmentProvisioningService provisioning;
    private final UserService userService;
    private final CourseAccess access;
    private final EnrollmentMapper enrollmentMapper;

    private final CoursePrerequisiteService prerequisites;

    public WaitlistServiceImplementation(WaitlistRepository waitlist, CourseRepository courses,
                                         CourseSectionRepository sections, EnrollmentRepository enrollments,
                                         UserRepository users, EnrollmentProvisioningService provisioning,
                                         UserService userService, CourseAccess access, EnrollmentMapper enrollmentMapper,
                                           CoursePrerequisiteService prerequisites) {
        this.prerequisites = prerequisites;
        this.waitlist = waitlist;
        this.courses = courses;
        this.sections = sections;
        this.enrollments = enrollments;
        this.users = users;
        this.provisioning = provisioning;
        this.userService = userService;
        this.access = access;
        this.enrollmentMapper = enrollmentMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public List<WaitlistDto> list(UUID sectionId) {
        Access.requireTenant();
        CourseSection s = sections.findById(sectionId).orElseThrow(() -> ApiException.notFound("Section"));
        access.requireRosterViewer(s);
        return toDtos(waitlist.findBySectionIdOrderByPositionAsc(sectionId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<WaitlistDto> mine() {
        Access.requireTenant();
        return toDtos(waitlist.findByUserIdOrderByDateAddedDesc(Access.ctx().userId()));
    }

    @Transactional
    @Override
    public WaitlistDto join(UUID sectionId, JoinWaitlistRequest r) {
        Access.requireTenant();
        CourseSection s = provisioning.lockSection(sectionId);
        Course c = course(s.getCourseId());
        UUID userId = r == null || r.userId() == null ? Access.ctx().userId() : r.userId();
        if (Access.isSelf(userId)) {
            if (c.getCourseStatus() != CourseStatus.PUBLISHED || s.getStatus() != CourseSectionStatus.ACTIVE
                    || Boolean.FALSE.equals(s.getIsOpenForEnrollment())) {
                throw ApiException.conflict("ENROLLMENT_CLOSED", "This section is not open for enrollment");
            }
        } else {
            access.requireRosterManager(s);
        }
        users.findById(userId).filter(u -> !Boolean.TRUE.equals(u.getIsSystemUser())).orElseThrow(() -> ApiException.notFound("User"));
        if (enrollments.existsBySectionIdAndUserIdAndEnrollmentStatus(sectionId, userId, EnrollmentStatus.ACTIVE)) {
            throw ApiException.conflict("ALREADY_ENROLLED", "The user is already enrolled in this section");
        }
        prerequisites.requireMet(c.getCourseId(), userId);
        Integer free = provisioning.openSeats(s);
        if (free == null || free > 0) throw ApiException.conflict("SEATS_AVAILABLE", "The section has free seats; enroll instead");
        Waitlist w = waitlist.findBySectionIdAndUserId(sectionId, userId).orElse(null);
        if (w != null && EnrollmentProvisioningServiceImplementation.OPEN_WAITLIST.contains(w.getWaitlistStatus())) {
            throw ApiException.conflict("ALREADY_WAITLISTED", "The user is already on this waitlist");
        }
        if (w == null) {
            w = new Waitlist();
            w.setSectionId(sectionId);
            w.setUserId(userId);
        }
        w.setPosition(waitlist.maxPosition(sectionId) + 1);         // to the back of the line
        w.setWaitlistStatus(WaitlistStatus.WAITING);
        w.setDateAdded(LocalDate.now());
        w.setDateOffered(null);
        w.setDateOfferedResponse(null);
        w.setOfferResponse(null);
        w.setAutoEnrollWhenAvailable(r == null || r.autoEnrollWhenAvailable() == null || r.autoEnrollWhenAvailable());
        waitlist.saveAndFlush(w);
        provisioning.recount(s);
        return toDto(w);
    }

    @Transactional
    @Override
    public WaitlistDto leave(UUID waitlistId) {
        Waitlist w = load(waitlistId);
        CourseSection s = authorizeOwnerOrManager(w);
        if (!EnrollmentProvisioningServiceImplementation.OPEN_WAITLIST.contains(w.getWaitlistStatus())) {
            throw ApiException.conflict("NOT_WAITING", "This place is " + w.getWaitlistStatus());
        }
        boolean heldSeat = w.getWaitlistStatus() == WaitlistStatus.OFFERED;
        w.setWaitlistStatus(WaitlistStatus.DECLINED);
        w.setOfferResponse("LEFT");
        w.setDateOfferedResponse(OffsetDateTime.now());
        waitlist.saveAndFlush(w);
        if (heldSeat) provisioning.fillOpenSeats(s, course(s.getCourseId()));
        else provisioning.recount(s);
        return toDto(w);
    }

    @Transactional
    @Override
    public EnrollmentDto accept(UUID waitlistId) {
        Waitlist w = load(waitlistId);
        CourseSection s = authorizeOwnerOrManager(w);
        if (w.getWaitlistStatus() != WaitlistStatus.OFFERED) throw ApiException.conflict("NO_OFFER", "There is no open offer");
        Course c = course(s.getCourseId());
        Enrollment e = provisioning.enroll(s, c, w.getUserId(), EnrollmentType.STUDENT, null, EnrollmentSource.SELF, null, null);
        UserSummaryDto u = userService.summariesById(List.of(e.getUserId())).get(e.getUserId());
        return enrollmentMapper.toDto(e, c.getCourseId(), u == null ? null : u.username(), u == null ? null : u.displayName());
    }

    @Transactional
    @Override
    public WaitlistDto decline(UUID waitlistId) {
        Waitlist w = load(waitlistId);
        CourseSection s = authorizeOwnerOrManager(w);
        if (w.getWaitlistStatus() != WaitlistStatus.OFFERED) throw ApiException.conflict("NO_OFFER", "There is no open offer");
        w.setWaitlistStatus(WaitlistStatus.DECLINED);
        w.setOfferResponse("DECLINED");
        w.setDateOfferedResponse(OffsetDateTime.now());
        waitlist.saveAndFlush(w);
        provisioning.fillOpenSeats(s, course(s.getCourseId()));      // the seat goes to the next in line
        return toDto(w);
    }

    // ---------------------------------------------------------------- helpers

    private CourseSection authorizeOwnerOrManager(Waitlist w) {
        Access.requireTenant();
        CourseSection s = provisioning.lockSection(w.getSectionId());
        if (!Access.isSelf(w.getUserId())) access.requireRosterManager(s);
        return s;
    }

    private List<WaitlistDto> toDtos(List<Waitlist> list) {
        Map<UUID, UserSummaryDto> names = userService.summariesById(list.stream().map(Waitlist::getUserId).toList());
        return list.stream().map(w -> {
            UserSummaryDto u = names.get(w.getUserId());
            return enrollmentMapper.toDto(w, u == null ? null : u.username(), u == null ? null : u.displayName());
        }).toList();
    }

    private WaitlistDto toDto(Waitlist w) {
        return toDtos(List.of(w)).get(0);
    }

    private Waitlist load(UUID id) {
        return waitlist.findById(id).orElseThrow(() -> ApiException.notFound("Waitlist place"));
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }
}
