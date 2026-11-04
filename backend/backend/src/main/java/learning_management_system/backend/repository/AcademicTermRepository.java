package learning_management_system.backend.repository;

import learning_management_system.backend.entity.AcademicTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicTermRepository extends JpaRepository<AcademicTerm, UUID> {

    List<AcademicTerm> findAllByOrderByDateStartDesc();

    Optional<AcademicTerm> findFirstByIsCurrentTrue();

    /** Only one term per institution is "current"; RLS limits this to the caller's tenant. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update AcademicTerm t set t.isCurrent = false where t.isCurrent = true and t.termId <> :keep")
    int clearCurrentExcept(@Param("keep") UUID keep);
}
