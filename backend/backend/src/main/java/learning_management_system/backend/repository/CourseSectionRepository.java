package learning_management_system.backend.repository;

import jakarta.persistence.LockModeType;
import learning_management_system.backend.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, UUID> {

    List<CourseSection> findByCourseIdOrderBySectionNumberAsc(UUID courseId);

    long countByCourseId(UUID courseId);

    /** Row lock (SELECT ... FOR UPDATE) so seat counts stay right when many students enroll at once. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from CourseSection s where s.sectionId = :id")
    Optional<CourseSection> lockById(@Param("id") UUID id);
}
