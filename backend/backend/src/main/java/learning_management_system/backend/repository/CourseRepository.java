package learning_management_system.backend.repository;

import learning_management_system.backend.entity.Course;
import learning_management_system.backend.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findBySelfEnrollmentCode(String code);

    /** Course catalog search for administrators; RLS already restricts rows to the caller's tenant. */
    @Query("""
        select c from Course c
        where (:termId is null or c.termId = :termId)
          and (:status is null or c.courseStatus = :status)
          and (:categoryId is null or c.categoryId = :categoryId)
          and (:orgUnitId is null or c.orgUnitId = :orgUnitId)
          and (:q is null or lower(c.courseCode) like lower(concat('%', cast(:q as string), '%'))
                          or lower(c.courseTitle) like lower(concat('%', cast(:q as string), '%')))
        """)
    Page<Course> search(@Param("termId") UUID termId, @Param("status") CourseStatus status, @Param("categoryId") UUID categoryId,
                        @Param("orgUnitId") UUID orgUnitId, @Param("q") String q, Pageable pageable);
}
