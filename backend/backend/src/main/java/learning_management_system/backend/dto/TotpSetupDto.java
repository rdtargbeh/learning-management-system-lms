package learning_management_system.backend.dto;

/** Authenticator-app setup: show otpauthUri as a QR code (or the secret for typing in), then confirm with a code. */
public record TotpSetupDto(String secret, String otpauthUri) {}
