package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import learning_management_system.backend.enums.SystemResourceLibraryResourceType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A shared resource (table system_resource_library): a course template, widget, tool, asset ... scope is PLATFORM
 * (offered to every institution by platform staff) or INSTITUTION.
 */
public record SystemResourceDto(UUID resourceId, String scope, String resourceName, SystemResourceLibraryResourceType resourceType,
                                String resourceUrl, String resourceDescription, UUID creatorUserId, Boolean isPublic, Boolean isApproved,
                                OffsetDateTime dateApproved, UUID approvedBy, Integer usageCount, Integer downloadCount, BigDecimal rating,
                                JsonNode tags, JsonNode metadata, OffsetDateTime dateCreated) {}
