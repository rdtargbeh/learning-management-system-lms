package learning_management_system.backend.repository;

import learning_management_system.backend.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, UUID> {

    List<Accommodation> findByUserIdOrderByDateCreatedDesc(UUID userId);

    List<Accommodation> findByUserIdInAndIsActiveTrue(Collection<UUID> userIds);
}
