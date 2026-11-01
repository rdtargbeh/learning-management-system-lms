package learning_management_system.backend.dto;

import java.util.List;
import java.util.UUID;

/** Access token plus the signed-in user. */
public record LoginResponse(String accessToken, String tokenType, long expiresIn, UserSummaryDto user,
                            List<String> permissions, String platformRole, UUID supportAccessId) {}
