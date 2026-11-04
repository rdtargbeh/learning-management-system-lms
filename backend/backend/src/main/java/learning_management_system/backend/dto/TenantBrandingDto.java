package learning_management_system.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** An institution's look (table tenant_branding). As a PUT body it replaces the branding; colors are #RRGGBB. */
public record TenantBrandingDto(@Size(max = 500) String logoUrl, @Size(max = 500) String faviconUrl, @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String primaryColor,
                                @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String secondaryColor, @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String accentColor, @Size(max = 100) String fontFamily,
                                @Size(max = 20000) String customCss, @Size(max = 500) String organizationTagline) {}
