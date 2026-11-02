package learning_management_system.backend.service;

import learning_management_system.backend.dto.StudentProfileDto;

import java.util.UUID;

/** student_profile: a student's academic record fields (student number, program, standing ...). USER_MANAGE edits. */
public interface StudentProfileService {

    /** The user themself, or anyone with USER_READ. */
    StudentProfileDto get(UUID userId);

    /** PATCH: null values leave a field unchanged. */
    StudentProfileDto update(UUID userId, StudentProfileDto d);
}
