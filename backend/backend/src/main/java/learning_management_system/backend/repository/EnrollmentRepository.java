package learning_management_system.backend.repository;

import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.EnrollmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    Optional<Enrollment> findBySectionIdAndUserId(UUID sectionId, UUID userId);

    List<Enrollment> findByUserIdOrderByDateEnrolledDesc(UUID userId);

    boolean existsBySectionIdAndUserIdAndEnrollmentStatus(UUID sectionId, UUID userId, EnrollmentStatus status);

    /** Seats in use: active enrollments of the seat-taking types. */
    long countBySectionIdAndEnrollmentStatusAndEnrollmentTypeIn(UUID sectionId, EnrollmentStatus status,
                                                                Collection<EnrollmentType> types);

    @Query("""
        select e from Enrollment e
        where e.sectionId = :sectionId
          and (:status is null or e.enrollmentStatus = :status)
          and (:type is null or e.enrollmentType = :type)
        order by e.dateEnrolled, e.enrollmentId
        """)
    List<Enrollment> roster(@Param("sectionId") UUID sectionId, @Param("status") EnrollmentStatus status,
                            @Param("type") EnrollmentType type);

    /** Active enrollment (any type) in any section of the course. */
    @Query("""
        select count(e) > 0 from Enrollment e, CourseSection s
        where e.sectionId = s.sectionId and s.courseId = :courseId and e.userId = :userId
          and e.enrollmentStatus = learning_management_system.backend.enums.EnrollmentStatus.ACTIVE
        """)
    boolean isActiveInCourse(@Param("courseId") UUID courseId, @Param("userId") UUID userId);

    /** The user's active enrollments (any type) across the sections of a course. */
    @Query("""
        select e from Enrollment e, CourseSection s
        where e.sectionId = s.sectionId and s.courseId = :courseId and e.userId = :userId
          and e.enrollmentStatus = learning_management_system.backend.enums.EnrollmentStatus.ACTIVE
        """)
    List<Enrollment> activeInCourse(@Param("courseId") UUID courseId, @Param("userId") UUID userId);

    /** Active enrollments of the given types in every section of a course (e.g. all students). */
    @Query("""
        select e from Enrollment e, CourseSection s
        where e.sectionId = s.sectionId and s.courseId = :courseId and e.enrollmentType in :types
          and e.enrollmentStatus = learning_management_system.backend.enums.EnrollmentStatus.ACTIVE
        order by e.dateEnrolled
        """)
    List<Enrollment> activeInCourseOfTypes(@Param("courseId") UUID courseId, @Param("types") Collection<EnrollmentType> types);

    /** The user's enrollments in any section of a course with one of these statuses (prerequisite checks). */
    @Query("""
        select e from Enrollment e, CourseSection s
        where e.sectionId = s.sectionId and s.courseId = :courseId and e.userId = :userId and e.enrollmentStatus in :statuses
        """)
    List<Enrollment> inCourseWithStatuses(@Param("courseId") UUID courseId, @Param("userId") UUID userId,
                                          @Param("statuses") Collection<EnrollmentStatus> statuses);
}
