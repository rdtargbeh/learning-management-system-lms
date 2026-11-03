package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/** Students to tag; students who already carry the tag are skipped. */
public record AddTagMembersRequest(@NotEmpty @Size(max = 500) List<UUID> userIds) {}
