package learning_management_system.backend.repository;

import learning_management_system.backend.entity.LtiProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/** Generated repository; add custom queries in a hand-written repository if needed. */
@Repository
public interface LtiProviderRepository extends JpaRepository<LtiProvider, UUID> {
}
