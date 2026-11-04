package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Create a group set. allowSelfSignup lets students pick their own group; restrictToSection keeps groups within
 * one section.
 */
public record CreateGroupSetRequest(@NotBlank @Size(max = 255) String setName, Boolean allowSelfSignup, Boolean restrictToSection,
                                    @Positive Integer maxMembersPerGroup, Boolean autoAssignLeader) {}
