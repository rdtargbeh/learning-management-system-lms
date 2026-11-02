package learning_management_system.backend.dto;

import java.util.List;

/** One-time backup codes, shown only once. Each works a single time when the authenticator is not at hand. */
public record BackupCodesDto(List<String> backupCodes) {}
