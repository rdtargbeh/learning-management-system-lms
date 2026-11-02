package learning_management_system.backend.repository;

import learning_management_system.backend.entity.AccountLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** One row per user (user_id is unique). */
@Repository
public interface AccountLockRepository extends JpaRepository<AccountLock, UUID> {

    Optional<AccountLock> findByUserId(UUID userId);
}
