package learning_management_system.backend.repository;

import learning_management_system.backend.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    List<RolePermission> findByRoleId(UUID roleId);
    void deleteByRoleId(UUID roleId);

    @Query("select p.permissionCode from RolePermission rp join Permission p on p.permissionId = rp.permissionId where rp.roleId in :roleIds and p.isActive = true")
    List<String> findPermissionCodesForRoles(@Param("roleIds") List<UUID> roleIds);
}
