package learning_management_system.backend.repository;

import learning_management_system.backend.entity.GroupSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupSetRepository extends JpaRepository<GroupSet, UUID> {

    List<GroupSet> findByCourseIdOrderBySetNameAsc(UUID courseId);

    boolean existsByCourseIdAndSetNameIgnoreCase(UUID courseId, String setName);
}
