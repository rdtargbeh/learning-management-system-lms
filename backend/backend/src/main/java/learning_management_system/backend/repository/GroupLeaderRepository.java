package learning_management_system.backend.repository;

import learning_management_system.backend.entity.GroupLeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupLeaderRepository extends JpaRepository<GroupLeader, UUID> {
    List<GroupLeader> findByGroupIdOrderByAppointedAtAsc(UUID groupId);

    /** Leaders whose term has not ended. */
    @Query("select l from GroupLeader l where l.groupId = :groupId and (l.termEndAt is null or l.termEndAt > :now) order by l.appointedAt")
    List<GroupLeader> current(@Param("groupId") UUID groupId, @Param("now") java.time.OffsetDateTime now);

    default List<GroupLeader> current(UUID groupId) {
        return current(groupId, java.time.OffsetDateTime.now());
    }

    Optional<GroupLeader> findByLeaderIdAndGroupId(UUID leaderId, UUID groupId);
}
