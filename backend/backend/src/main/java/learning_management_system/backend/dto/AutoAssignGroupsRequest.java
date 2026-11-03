package learning_management_system.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

/**
 * Puts every enrolled student who is not in a group of the set into one, at random, filling the smallest groups first.
 * groupCount: make sure the set has at least this many active groups (per section when the set is restricted to
 * sections), creating "<set name> N" groups as needed.
 */
public record AutoAssignGroupsRequest(@Positive @Max(200) Integer groupCount) {}
