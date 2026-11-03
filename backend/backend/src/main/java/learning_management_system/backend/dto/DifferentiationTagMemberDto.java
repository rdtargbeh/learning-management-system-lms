package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A student carrying a differentiation tag (table differentiation_tag_member). */
public record DifferentiationTagMemberDto(UUID tagMemberId, UUID tagId, UUID userId, String username, String displayName,
                                          OffsetDateTime dateCreated) {}
