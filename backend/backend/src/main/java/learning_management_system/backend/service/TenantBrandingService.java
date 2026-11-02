package learning_management_system.backend.service;

import learning_management_system.backend.dto.PublicBrandingDto;
import learning_management_system.backend.dto.TenantBrandingDto;

/** An institution's look: logo, colors, font, tagline and custom CSS. */
public interface TenantBrandingService {

    /** The caller's institution's branding (empty values when none is set). */
    TenantBrandingDto get();

    /** Replaces the branding (TENANT_SETTINGS_MANAGE). */
    TenantBrandingDto replace(TenantBrandingDto d);

    /** For the sign-in page, without signing in: branding of an active institution by its code. */
    PublicBrandingDto publicBranding(String tenantCode);
}
