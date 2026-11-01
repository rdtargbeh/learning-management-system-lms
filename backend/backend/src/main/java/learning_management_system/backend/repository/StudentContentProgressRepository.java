package learning_management_system.backend.repository;

import learning_management_system.backend.entity.StudentContentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentContentProgressRepository extends JpaRepository<StudentContentProgress, UUID> {

    Optional<StudentContentProgress> findByUserIdAndContentId(UUID userId, UUID contentId);

    List<StudentContentProgress> findByUserIdAndContentIdIn(UUID userId, Collection<UUID> contentIds);

    List<StudentContentProgress> findByUserIdInAndContentIdIn(Collection<UUID> userIds, Collection<UUID> contentIds);
}
