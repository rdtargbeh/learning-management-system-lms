package learning_management_system.backend.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** PATCH of a group set: null leaves a value unchanged. */
public record UpdateGroupSetRequest(@Size(min = 1, max = 255) String setName, Boolean allowSelfSignup, Boolean restrictToSection,
                                    @Positive Integer maxMembersPerGroup, Boolean autoAssignLeader) {}
