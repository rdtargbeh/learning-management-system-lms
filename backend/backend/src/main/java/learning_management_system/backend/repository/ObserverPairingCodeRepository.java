package learning_management_system.backend.repository;

import learning_management_system.backend.entity.ObserverPairingCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ObserverPairingCodeRepository extends JpaRepository<ObserverPairingCode, UUID> {
    Optional<ObserverPairingCode> findFirstByCodeHash(String codeHash);

    List<ObserverPairingCode> findByStudentUserIdOrderByDateCreatedDesc(UUID studentUserId);

    /** Ends the student's unused codes (a new code replaces them). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update ObserverPairingCode c set c.dateExpires = :now where c.studentUserId = :studentUserId and c.usedBy is null and c.dateExpires > :now")
    int expireOpen(@Param("studentUserId") UUID studentUserId, @Param("now") OffsetDateTime now);
}
