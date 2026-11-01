package learning_management_system.backend.repository;

import learning_management_system.backend.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, UUID> {
    List<Institution> findByTenantId(UUID tenantId);
}
