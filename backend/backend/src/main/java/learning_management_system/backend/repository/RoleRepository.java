package learning_management_system.backend.repository;

import learning_management_system.backend.enums.RoleLevel;
import learning_management_system.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    List<Role> findByRoleLevelOrderByRoleName(RoleLevel roleLevel);
    Optional<Role> findByRoleCodeAndTenantIdIsNull(String roleCode);
}
