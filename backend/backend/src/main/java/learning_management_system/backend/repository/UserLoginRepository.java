package learning_management_system.backend.repository;

import learning_management_system.backend.entity.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLoginRepository extends JpaRepository<UserLogin, UUID> {

    List<UserLogin> findByUserIdOrderByDateCreatedAsc(UUID userId);

    Optional<UserLogin> findFirstByTenantIdAndUniqueLoginIgnoreCaseAndIsActiveTrue(UUID tenantId, String uniqueLogin);
}
