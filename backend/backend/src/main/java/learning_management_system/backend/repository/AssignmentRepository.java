package learning_management_system.backend.repository;

import learning_management_system.backend.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    List<Assignment> findByCourseIdOrderByDateDueAscAssignmentTitleAsc(UUID courseId);

    List<Assignment> findByModuleIdIn(Collection<UUID> moduleIds);

    /** Assignments and content share one order inside a module. */
    @Query("select coalesce(max(a.displayOrder), 0) from Assignment a where a.moduleId = :moduleId")
    int maxDisplayOrderInModule(@Param("moduleId") UUID moduleId);
}
