package learning_management_system.backend.repository;

import learning_management_system.backend.entity.UserSession;
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
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    List<UserSession> findByUserIdAndIsActiveTrueOrderByLastActivityDesc(UUID userId);

    /** Sign the user out everywhere (password change, deactivation, lock). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update UserSession s set s.isActive = false, s.revokedAt = :now where s.userId = :userId and s.isActive = true")
    int revokeAllForUser(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);
}
