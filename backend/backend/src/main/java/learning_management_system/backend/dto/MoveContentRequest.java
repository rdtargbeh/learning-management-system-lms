package learning_management_system.backend.dto;

import java.util.UUID;

/** Move an item into a module (at the end), or out of every module with moduleId = null. */
public record MoveContentRequest(UUID moduleId) {}
