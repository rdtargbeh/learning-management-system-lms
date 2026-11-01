package learning_management_system.backend.repository;

import learning_management_system.backend.entity.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, UUID> {

    List<CourseModule> findByCourseIdOrderByDisplayOrderAscModuleNumberAsc(UUID courseId);

    @Query("select coalesce(max(m.moduleNumber), 0) from CourseModule m where m.courseId = :courseId")
    int maxModuleNumber(@Param("courseId") UUID courseId);
}
