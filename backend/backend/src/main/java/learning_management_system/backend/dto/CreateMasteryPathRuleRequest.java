package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** A mastery path on a graded (points) assignment of the course; one path per assignment. */
public record CreateMasteryPathRuleRequest(@NotNull UUID triggerAssignmentId) {}
