package learning_management_system.backend.dto;

/** Branding shown on an institution's sign-in page before anyone signs in (no custom CSS). */
public record PublicBrandingDto(String tenantName, String tenantCode, String logoUrl, String faviconUrl, String primaryColor,
                                String secondaryColor, String accentColor, String fontFamily, String organizationTagline) {}
