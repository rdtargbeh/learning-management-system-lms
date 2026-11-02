package learning_management_system.backend.service;

import learning_management_system.backend.dto.AcademicAdvisorProfileDto;

import java.util.UUID;

/** academic_advisor_profile: advising details of an advisor (who also has an employee profile). USER_MANAGE edits. */
public interface AcademicAdvisorProfileService {

    /** The user themself, or anyone with USER_READ. */
    AcademicAdvisorProfileDto get(UUID userId);

    /** PATCH: null values leave a field unchanged. */
    AcademicAdvisorProfileDto update(UUID userId, AcademicAdvisorProfileDto d);
}
