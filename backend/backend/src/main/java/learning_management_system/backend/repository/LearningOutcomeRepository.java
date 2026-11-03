package learning_management_system.backend.repository;

import learning_management_system.backend.entity.LearningOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface LearningOutcomeRepository extends JpaRepository<LearningOutcome, UUID> {
    List<LearningOutcome> findByCourseIdOrderByOutcomeNumberAsc(UUID courseId);

    List<LearningOutcome> findByCourseIdIsNullAndOrgUnitIdInOrderByOutcomeNumberAsc(Collection<UUID> orgUnitIds);

    boolean existsByCourseIdAndOutcomeNumber(UUID courseId, Integer outcomeNumber);

    boolean existsByCourseIdIsNullAndOrgUnitIdAndOutcomeNumber(UUID orgUnitId, Integer outcomeNumber);

    @Query("select coalesce(max(o.outcomeNumber), 0) from LearningOutcome o where o.courseId = :courseId")
    int maxNumberInCourse(@Param("courseId") UUID courseId);

    @Query("select coalesce(max(o.outcomeNumber), 0) from LearningOutcome o where o.courseId is null and o.orgUnitId = :orgUnitId")
    int maxNumberInOrgUnit(@Param("orgUnitId") UUID orgUnitId);
}
