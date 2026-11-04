package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Size;
import learning_management_system.backend.enums.BlockType;

/** Adds a block (blockType required; it goes at the end) or changes one (PATCH: null leaves a value unchanged). */
public record ContentBlockRequest(BlockType blockType, @Size(max = 255) String blockTitle, @Size(max = 200000) String blockContent,
                                  JsonNode blockContentHtml, JsonNode metadata) {}
