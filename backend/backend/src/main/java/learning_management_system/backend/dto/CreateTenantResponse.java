package learning_management_system.backend.dto;

/** The new institution and its first administrator. */
public record CreateTenantResponse(TenantDto tenant, UserSummaryDto admin) {}
