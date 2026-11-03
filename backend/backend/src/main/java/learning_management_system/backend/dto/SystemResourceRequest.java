package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import learning_management_system.backend.enums.SystemResourceLibraryResourceType;

import java.math.BigDecimal;

/**
 * Adds a resource (resourceName and resourceType required) or changes one (PATCH). For institution resources isPublic
 * means students may see it too; platform resources need isPublic to be offered to institutions. rating (1.0-5.0) is set
 * by library managers. tags is a JSON array of strings.
 */
public record SystemResourceRequest(@Size(min = 3, max = 255) String resourceName, SystemResourceLibraryResourceType resourceType,
                                    @Size(max = 500) @Pattern(regexp = "^https?://.+") String resourceUrl, String resourceDescription,
                                    Boolean isPublic, @DecimalMin("1.0") @DecimalMax("5.0") BigDecimal rating, JsonNode tags, JsonNode metadata) {}
