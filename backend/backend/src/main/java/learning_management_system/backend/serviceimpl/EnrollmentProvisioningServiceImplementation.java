package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseSection;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.entity.Waitlist;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.CourseSectionStatus;
import learning_management_system.backend.enums.CourseStatus;
import learning_management_system.backend.enums.EnrollmentSource;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.EnrollmentType;
import learning_management_system.backend.enums.WaitlistStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.CourseInstructorRepository;
import learning_management_system.backend.repository.CourseSectionRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.repository.WaitlistRepository;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.EnrollmentProvisioningService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
public class EnrollmentProvisioningServiceImplementation implements EnrollmentProvisioningService {
    /** Enrollment types that take a seat. Observers and former students do not. */
    static final Set<EnrollmentType> SEAT_TYPES = EnumSet.of(EnrollmentType.STUDENT, EnrollmentType.AUDIT, EnrollmentType.VISITING);
    static final Set<WaitlistStatus> OPEN_WAITLIST = EnumSet.of(WaitlistStatus.WAITING, WaitlistStatus.OFFERED);

    private final CourseSectionRepository sections;
    private final EnrollmentRepository enrollments;
    private final WaitlistRepository waitlist;
    private final UserRepository users;
    private final CourseInstructorRepository staff;
    private final EntityRefresher refresher;

    public EnrollmentProvisioningServiceImplementation(CourseSectionRepository sections, EnrollmentRepository enrollments,
                                                       WaitlistRepository waitlist, UserRepository users,
                                                       CourseInstructorRepository staff, EntityRefresher refresher) {
        this.sections = sections;
        this.enrollments = enrollments;
        this.waitlist = waitlist;
        this.users = users;
        this.staff = staff;
        this.refresher = refresher;
    }

    @Override
    public CourseSection lockSection(UUID sectionId) {
        return sections.lockById(sectionId).orElseThrow(() -> ApiException.notFound("Section"));
    }

    @Override
    public Enrollment enroll(CourseSection s, Course course, UUID userId, EnrollmentType type, UUID observedUserId,
                             EnrollmentSource source, Boolean limitToSection, String sisSourceId) {
        EnrollmentType t = type == null ? EnrollmentType.STUDENT : type;
        if (t == EnrollmentType.WITHDRAWN_STUDENT) {
            throw ApiException.badRequest("INVALID_ENROLLMENT_TYPE", "WITHDRAWN_STUDENT is set by withdrawing, not by enrolling");
        }
        if (Boolean.TRUE.equals(course.getIsArchived()) || course.getCourseStatus() == CourseStatus.ARCHIVED
                || course.getCourseStatus() == CourseStatus.CONCLUDED) {
            throw ApiException.conflict("COURSE_CLOSED", "The course is " + course.getCourseStatus());
        }
        if (s.getStatus() == CourseSectionStatus.CANCELLED || s.getStatus() == CourseSectionStatus.COMPLETED) {
            throw ApiException.conflict("SECTION_CLOSED", "The section is " + s.getStatus());
        }
        User user = users.findById(userId).filter(u -> !Boolean.TRUE.equals(u.getIsSystemUser()))
                .orElseThrow(() -> ApiException.notFound("User"));
        if (user.getAccountStatus() == AccountStatus.DEACTIVATED || user.getAccountStatus() == AccountStatus.DELETED) {
            throw ApiException.conflict("USER_INACTIVE", "The user's account is " + user.getAccountStatus());
        }
        if (t == EnrollmentType.OBSERVER) {
            if (observedUserId == null) throw ApiException.badRequest("OBSERVED_USER_REQUIRED", "An observer enrollment needs observedUserId");
            Enrollment observed = enrollments.findBySectionIdAndUserId(s.getSectionId(), observedUserId)
                    .filter(e -> e.getEnrollmentStatus() == EnrollmentStatus.ACTIVE && SEAT_TYPES.contains(e.getEnrollmentType()))
                    .orElseThrow(() -> ApiException.badRequest("OBSERVED_NOT_ENROLLED", "The observed student is not enrolled in this section"));
            if (observed.getUserId().equals(userId)) throw ApiException.badRequest("SELF_OBSERVE", "A user cannot observe themselves");
        } else if (observedUserId != null) {
            throw ApiException.badRequest("OBSERVED_USER_NOT_ALLOWED", "observedUserId is only for OBSERVER enrollments");
        }
        if (SEAT_TYPES.contains(t) && staff.isStaffInSection(s.getSectionId(), userId, CourseAccess.STAFF_ROLES)) {
            throw ApiException.conflict("USER_IS_STAFF", "The user is on this section's staff");
        }
        Enrollment e = enrollments.findBySectionIdAndUserId(s.getSectionId(), userId).orElse(null);
        if (e != null && e.getEnrollmentStatus() == EnrollmentStatus.ACTIVE) {
            throw ApiException.conflict("ALREADY_ENROLLED", "The user is already enrolled in this section");
        }
        Waitlist place = waitlist.findBySectionIdAndUserId(s.getSectionId(), userId)
                .filter(w -> OPEN_WAITLIST.contains(w.getWaitlistStatus())).orElse(null);
        if (SEAT_TYPES.contains(t) && s.getCapacity() != null) {
            long used = seatsUsed(s);
            if (place != null && place.getWaitlistStatus() == WaitlistStatus.OFFERED) used--;   // their own offer holds a seat
            if (used >= s.getCapacity()) {
                throw ApiException.conflict("SECTION_FULL", "The section is full; join the waitlist instead");
            }
        }
        if (place != null) {
            // Mark the waitlist place first: the database refuses waitlist changes for actively enrolled users.
            if (place.getWaitlistStatus() == WaitlistStatus.OFFERED) {
                place.setOfferResponse("ACCEPTED");
                place.setDateOfferedResponse(OffsetDateTime.now());
            }
            place.setWaitlistStatus(WaitlistStatus.ENROLLED);
            waitlist.saveAndFlush(place);
        }
        if (e == null) {
            e = new Enrollment();
            e.setSectionId(s.getSectionId());
            e.setUserId(userId);
        }
        e.setEnrollmentType(t);
        e.setObservedUserId(observedUserId);
        e.setEnrollmentStatus(EnrollmentStatus.ACTIVE);
        e.setEnrollmentSource(source == null ? EnrollmentSource.MANUAL : source);
        if (limitToSection != null) e.setLimitToSection(limitToSection);
        if (sisSourceId != null) e.setSisSourceId(sisSourceId);
        e.setDateEnrolled(LocalDate.now());
        e.setDateWithdrawn(null);
        e.setDateCompleted(null);
        refresher.refresh(enrollments.saveAndFlush(e));
        recount(s);
        return e;
    }

    @Override
    public Enrollment changeStatus(CourseSection s, Course course, Enrollment e, EnrollmentStatus status) {
        if (status == e.getEnrollmentStatus()) return e;
        if (status == EnrollmentStatus.ACTIVE) {
            EnrollmentType t = e.getEnrollmentType() == EnrollmentType.WITHDRAWN_STUDENT ? EnrollmentType.STUDENT : e.getEnrollmentType();
            return enroll(s, course, e.getUserId(), t, e.getObservedUserId(), e.getEnrollmentSource(), e.getLimitToSection(), null);
        }
        e.setEnrollmentStatus(status);
        LocalDate today = LocalDate.now();
        switch (status) {
            case WITHDRAWN, DROPPED -> {
                e.setDateWithdrawn(today.isBefore(e.getDateEnrolled()) ? e.getDateEnrolled() : today);
                if (status == EnrollmentStatus.WITHDRAWN && e.getEnrollmentType() == EnrollmentType.STUDENT) {
                    e.setEnrollmentType(EnrollmentType.WITHDRAWN_STUDENT);
                }
            }
            case COMPLETED -> e.setDateCompleted(today.isBefore(e.getDateEnrolled()) ? e.getDateEnrolled() : today);
            default -> { }
        }
        enrollments.saveAndFlush(e);
        fillOpenSeats(s, course);
        return e;
    }

    @Override
    public Integer openSeats(CourseSection s) {
        if (s.getCapacity() == null) return null;
        return (int) Math.max(0, s.getCapacity() - seatsUsed(s));
    }

    @Override
    public void fillOpenSeats(CourseSection s, Course course) {
        boolean open = s.getStatus() != CourseSectionStatus.CANCELLED && s.getStatus() != CourseSectionStatus.COMPLETED
                && course.getCourseStatus() != CourseStatus.CONCLUDED && course.getCourseStatus() != CourseStatus.ARCHIVED;
        if (open) {
            for (Waitlist w : waitlist.findBySectionIdAndWaitlistStatusInOrderByPositionAsc(s.getSectionId(), EnumSet.of(WaitlistStatus.WAITING))) {
                Integer free = openSeats(s);
                if (free != null && free <= 0) break;
                if (w.getAutoEnrollWhenAvailable() == null || w.getAutoEnrollWhenAvailable()) {
                    try {
                        enroll(s, course, w.getUserId(), EnrollmentType.STUDENT, null, EnrollmentSource.SELF, null, null);
                    } catch (ApiException ex) {             // e.g. account deactivated since joining: skip them
                        w.setWaitlistStatus(WaitlistStatus.EXPIRED);
                        waitlist.saveAndFlush(w);
                    }
                } else {
                    w.setWaitlistStatus(WaitlistStatus.OFFERED);
                    w.setDateOffered(OffsetDateTime.now());
                    waitlist.saveAndFlush(w);
                }
            }
        }
        recount(s);
    }

    @Override
    public void recount(CourseSection s) {
        s.setEnrolledCount((int) enrollments.countBySectionIdAndEnrollmentStatusAndEnrollmentTypeIn(
                s.getSectionId(), EnrollmentStatus.ACTIVE, SEAT_TYPES));
        s.setWaitlistCount((int) waitlist.countBySectionIdAndWaitlistStatusIn(s.getSectionId(), OPEN_WAITLIST));
        sections.saveAndFlush(s);
    }

    /** Active seat-taking enrollments plus open offers. */
    private long seatsUsed(CourseSection s) {
        return enrollments.countBySectionIdAndEnrollmentStatusAndEnrollmentTypeIn(s.getSectionId(), EnrollmentStatus.ACTIVE, SEAT_TYPES)
                + waitlist.countBySectionIdAndWaitlistStatusIn(s.getSectionId(), EnumSet.of(WaitlistStatus.OFFERED));
    }
}
