package learning_management_system.backend.repository;

import learning_management_system.backend.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    List<GroupMember> findByGroupIdAndIsActiveTrueOrderByJoinedAtAsc(UUID groupId);

    long countByGroupIdAndIsActiveTrue(UUID groupId);

    /** The student's active membership in any group of the set (a student is in at most one group per set). */
    @Query("""
        select gm from GroupMember gm, StudentGroup g
        where gm.groupId = g.groupId and g.groupSetId = :groupSetId and gm.userId = :userId and gm.isActive = true
        """)
    Optional<GroupMember> activeInSet(@Param("groupSetId") UUID groupSetId, @Param("userId") UUID userId);

    /** Active memberships of a user in the groups of a course. */
    @Query("""
        select gm from GroupMember gm, StudentGroup g, GroupSet s
        where gm.groupId = g.groupId and g.groupSetId = s.groupSetId and s.courseId = :courseId and gm.userId = :userId and gm.isActive = true
        """)
    List<GroupMember> activeInCourse(@Param("courseId") UUID courseId, @Param("userId") UUID userId);

    /** Users with an active membership in any group of the set. */
    @Query("""
        select gm.userId from GroupMember gm, StudentGroup g
        where gm.groupId = g.groupId and g.groupSetId = :groupSetId and gm.isActive = true
        """)
    List<UUID> activeUserIdsInSet(@Param("groupSetId") UUID groupSetId);
}
