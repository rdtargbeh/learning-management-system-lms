package learning_management_system.backend.service;

import learning_management_system.backend.dto.LearningOutcomeDto;
import learning_management_system.backend.dto.LearningOutcomeRequest;

import java.util.List;
import java.util.UUID;

/**
 * learning_outcome: what students should be able to do, as in Canvas outcomes. Course outcomes are managed by the
 * course's teachers; org-unit (program, department) outcomes by ACADEMIC_STRUCTURE_MANAGE and inherited by the courses
 * of that unit and its sub-units. Students see active outcomes only.
 */
public interface LearningOutcomeService {

    /** includeInherited adds the outcomes of the course's org unit and every unit above it. */
    List<LearningOutcomeDto> forCourse(UUID courseId, boolean includeInherited, boolean includeInactive);

    LearningOutcomeDto createForCourse(UUID courseId, LearningOutcomeRequest r);

    List<LearningOutcomeDto> forOrgUnit(UUID orgUnitId, boolean includeInactive);

    LearningOutcomeDto createForOrgUnit(UUID orgUnitId, LearningOutcomeRequest r);

    LearningOutcomeDto get(UUID outcomeId);

    LearningOutcomeDto update(UUID outcomeId, LearningOutcomeRequest r);

    /** Only when no student results exist for it (409 OUTCOME_IN_USE: deactivate it instead). */
    void delete(UUID outcomeId);
}
