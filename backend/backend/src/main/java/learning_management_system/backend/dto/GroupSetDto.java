package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A set of groups in a course, e.g. "Project teams" (table group_set). */
public record GroupSetDto(UUID groupSetId, UUID courseId, String setName, Boolean allowSelfSignup, Boolean restrictToSection,
                          Integer maxMembersPerGroup, Boolean autoAssignLeader, OffsetDateTime dateCreated) {}
