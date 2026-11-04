package learning_management_system.backend.service;

import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseSection;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.enums.EnrollmentSource;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.EnrollmentType;

import java.util.UUID;

/**
 * The single code path that activates enrollments and keeps a section's seat counts right. Callers must already be
 * authorized and must hold the section row lock ({@link #lockSection}) inside their transaction.
 * <ul>
 *   <li>Seats are taken by ACTIVE enrollments of type STUDENT, AUDIT and VISITING, plus open waitlist offers.</li>
 *   <li>When a seat frees up the waitlist moves: the next student is enrolled automatically, or gets an offer.</li>
 * </ul>
 */
public interface EnrollmentProvisioningService {

    /** Loads the section with a row lock (SELECT ... FOR UPDATE). */
    CourseSection lockSection(UUID sectionId);

    /** Enrolls (or re-activates) a user; marks a waitlist place for them as ENROLLED. */
    Enrollment enroll(CourseSection lockedSection, Course course, UUID userId, EnrollmentType type, UUID observedUserId,
                      EnrollmentSource source, Boolean limitToSection, String sisSourceId);

    /** Changes the status; ACTIVE goes through {@link #enroll} so all checks apply. Frees seats for the waitlist. */
    Enrollment changeStatus(CourseSection lockedSection, Course course, Enrollment enrollment, EnrollmentStatus status);

    /** Seats still free: capacity minus seat-taking enrollments and open offers (null = unlimited). */
    Integer openSeats(CourseSection lockedSection);

    /** Gives free seats to the waitlist in order, then refreshes the section's counters. */
    void fillOpenSeats(CourseSection lockedSection, Course course);

    /** Recomputes enrolled_count and waitlist_count from the rows. */
    void recount(CourseSection lockedSection);
}
