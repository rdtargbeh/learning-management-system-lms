package learning_management_system.backend.dto;

import java.util.UUID;

/** A kind of content item, e.g. PAGE, VIDEO, FILE, LINK (table content_type). */
public record ContentTypeDto(UUID typeId, String typeName, String typeCode, String description, String iconUrl, Boolean isActive) {}
