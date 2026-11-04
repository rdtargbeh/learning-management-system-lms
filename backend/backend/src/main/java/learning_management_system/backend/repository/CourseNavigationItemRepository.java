package learning_management_system.backend.repository;

import learning_management_system.backend.entity.CourseNavigationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseNavigationItemRepository extends JpaRepository<CourseNavigationItem, UUID> {
    List<CourseNavigationItem> findByCourseIdOrderByDisplayOrderAsc(UUID courseId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CourseNavigationItem n where n.courseId = :courseId")
    int deleteByCourse(@Param("courseId") UUID courseId);
}
