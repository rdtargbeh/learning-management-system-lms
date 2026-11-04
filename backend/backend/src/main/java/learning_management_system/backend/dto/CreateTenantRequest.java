package learning_management_system.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Onboard an institution: tenant + settings + optional primary domain and institution record + first tenant admin. */
public record CreateTenantRequest(@NotBlank @Size(min = 2, max = 255) String tenantName,
                                  @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{2,50}$") String tenantCode,
                                  String tenantDescription, String dataRegion, String defaultTimezone, String defaultLocale,
                                  String primaryDomain, String institutionName, @NotNull @Valid InitialAdminRequest admin) {}
