package learning_management_system.backend.repository;

import learning_management_system.backend.entity.MfaConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** One row per user (user_id is unique). */
@Repository
public interface MfaConfigurationRepository extends JpaRepository<MfaConfiguration, UUID> {

    Optional<MfaConfiguration> findByUserId(UUID userId);
}
