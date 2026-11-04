package learning_management_system.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lms.security")
public record SecurityProperties(String jwtSecret, int tokenTtlMinutes, String issuer) {}
