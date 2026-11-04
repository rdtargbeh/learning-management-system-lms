package learning_management_system.backend.repository;

import learning_management_system.backend.entity.CourseInstructor;
import learning_management_system.backend.enums.CourseInstructorRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseInstructorRepository extends JpaRepository<CourseInstructor, UUID> {

    List<CourseInstructor> findBySectionIdOrderByRoleAsc(UUID sectionId);

    List<CourseInstructor> findByUserId(UUID userId);

    Optional<CourseInstructor> findBySectionIdAndUserId(UUID sectionId, UUID userId);

    /** Current (not ended) staff assignment with one of the roles in a section. */
    @Query("""
        select count(ci) > 0 from CourseInstructor ci
        where ci.sectionId = :sectionId and ci.userId = :userId and ci.role in :roles
          and (ci.dateEnd is null or ci.dateEnd >= current_date)
        """)
    boolean isStaffInSection(@Param("sectionId") UUID sectionId, @Param("userId") UUID userId,
                             @Param("roles") Collection<CourseInstructorRole> roles);

    /** Current staff assignment with one of the roles in any section of the course. */
    @Query("""
        select count(ci) > 0 from CourseInstructor ci, CourseSection s
        where ci.sectionId = s.sectionId and s.courseId = :courseId and ci.userId = :userId and ci.role in :roles
          and (ci.dateEnd is null or ci.dateEnd >= current_date)
        """)
    boolean isStaffInCourse(@Param("courseId") UUID courseId, @Param("userId") UUID userId,
                            @Param("roles") Collection<CourseInstructorRole> roles);
}
