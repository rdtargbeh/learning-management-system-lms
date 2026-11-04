package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import learning_management_system.backend.enums.BlockType;

import java.time.OffsetDateTime;
import java.util.UUID;

/** One block of a content page (table content_block): a piece of text, an image, code, an embed ... */
public record ContentBlockDto(UUID blockId, UUID contentId, Integer blockNumber, BlockType blockType, String blockTitle, String blockContent,
                              JsonNode blockContentHtml, Integer displayOrder, JsonNode metadata, OffsetDateTime dateUpdated) {}
