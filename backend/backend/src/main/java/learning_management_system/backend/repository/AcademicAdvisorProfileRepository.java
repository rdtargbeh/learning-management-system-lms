package learning_management_system.backend.repository;

import learning_management_system.backend.entity.AcademicAdvisorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicAdvisorProfileRepository extends JpaRepository<AcademicAdvisorProfile, UUID> {
    Optional<AcademicAdvisorProfile> findByUserId(UUID userId);
}
