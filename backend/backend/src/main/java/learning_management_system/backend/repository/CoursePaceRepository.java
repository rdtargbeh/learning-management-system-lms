package learning_management_system.backend.repository;

import learning_management_system.backend.entity.CoursePace;
import learning_management_system.backend.enums.PaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursePaceRepository extends JpaRepository<CoursePace, java.util.UUID> {
    List<CoursePace> findByCourseIdOrderByDateCreatedAsc(java.util.UUID courseId);

    List<CoursePace> findByCourseIdAndPaceStatus(java.util.UUID courseId, PaceStatus status);
}
