package learning_management_system.backend.repository;

import learning_management_system.backend.entity.DifferentiationTagMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DifferentiationTagMemberRepository extends JpaRepository<DifferentiationTagMember, UUID> {
    List<DifferentiationTagMember> findByTagIdOrderByDateCreatedAsc(UUID tagId);

    Optional<DifferentiationTagMember> findByTagIdAndUserId(UUID tagId, UUID userId);

    long countByTagId(UUID tagId);

    boolean existsByTagIdAndUserId(UUID tagId, UUID userId);
}
