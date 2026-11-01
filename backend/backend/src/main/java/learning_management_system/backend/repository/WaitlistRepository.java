package learning_management_system.backend.repository;

import learning_management_system.backend.entity.Waitlist;
import learning_management_system.backend.enums.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, UUID> {

    Optional<Waitlist> findBySectionIdAndUserId(UUID sectionId, UUID userId);

    List<Waitlist> findBySectionIdOrderByPositionAsc(UUID sectionId);

    List<Waitlist> findByUserIdOrderByDateAddedDesc(UUID userId);

    List<Waitlist> findBySectionIdAndWaitlistStatusInOrderByPositionAsc(UUID sectionId, Collection<WaitlistStatus> statuses);

    long countBySectionIdAndWaitlistStatusIn(UUID sectionId, Collection<WaitlistStatus> statuses);

    @Query("select coalesce(max(w.position), 0) from Waitlist w where w.sectionId = :sectionId")
    int maxPosition(@Param("sectionId") UUID sectionId);
}
