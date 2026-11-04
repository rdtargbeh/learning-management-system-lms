package learning_management_system.backend.repository;

import learning_management_system.backend.entity.ContentPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentPermissionRepository extends JpaRepository<ContentPermission, UUID> {
    List<ContentPermission> findByContentIdOrderByDateGrantedAsc(UUID contentId);

    Optional<ContentPermission> findByPermissionIdAndContentId(UUID permissionId, UUID contentId);
}
