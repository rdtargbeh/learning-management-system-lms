package learning_management_system.backend.repository;

import learning_management_system.backend.entity.DifferentiationTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DifferentiationTagRepository extends JpaRepository<DifferentiationTag, UUID> {
    List<DifferentiationTag> findByCourseIdOrderByTagNameAsc(UUID courseId);

    boolean existsByCourseIdAndTagNameIgnoreCase(UUID courseId, String tagName);
}
