package learning_management_system.backend.service;

import learning_management_system.backend.dto.EmployeeProfileDto;

import java.util.UUID;

/** employee_profile: faculty, TAs, advisors, staff and admins (employee number, type, department ...). USER_MANAGE edits. */
public interface EmployeeProfileService {

    /** The user themself, or anyone with USER_READ. */
    EmployeeProfileDto get(UUID userId);

    /** PATCH: null values leave a field unchanged. */
    EmployeeProfileDto update(UUID userId, EmployeeProfileDto d);
}
