package learning_management_system.backend.repository;

import learning_management_system.backend.entity.CoursePrerequisite;
import learning_management_system.backend.enums.PrerequisiteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, UUID> {
    List<CoursePrerequisite> findByCourseIdOrderByDateCreatedAsc(UUID courseId);

    List<CoursePrerequisite> findByCourseIdAndPrerequisiteType(UUID courseId, PrerequisiteType type);

    Optional<CoursePrerequisite> findByCourseIdAndPrerequisiteCourseId(UUID courseId, UUID prerequisiteCourseId);

    Optional<CoursePrerequisite> findByPrerequisiteIdAndCourseId(UUID prerequisiteId, UUID courseId);
}
