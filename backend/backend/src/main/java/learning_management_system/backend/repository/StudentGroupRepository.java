package learning_management_system.backend.repository;

import jakarta.persistence.LockModeType;
import learning_management_system.backend.entity.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, UUID> {

    List<StudentGroup> findByGroupSetIdOrderByGroupNameAsc(UUID groupSetId);

    /** Row lock so the member count cannot overshoot when several students join at once. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from StudentGroup g where g.groupId = :id")
    Optional<StudentGroup> lockById(@Param("id") UUID id);
}
