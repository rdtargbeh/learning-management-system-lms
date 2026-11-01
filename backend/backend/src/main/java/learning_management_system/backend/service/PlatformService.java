package learning_management_system.backend.service;

import learning_management_system.backend.dto.CreatePlatformUserRequest;
import learning_management_system.backend.dto.CreateTenantRequest;
import learning_management_system.backend.dto.CreateTenantResponse;
import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.PlatformSupportAccessDto;
import learning_management_system.backend.dto.PlatformUserProfileDto;
import learning_management_system.backend.dto.SupportAccessRequest;
import learning_management_system.backend.dto.TenantDto;
import learning_management_system.backend.dto.UpdateTenantRequest;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.dto.UserSummaryDto;

import java.util.List;
import java.util.UUID;

/** Platform-operator features: onboarding institutions, platform staff, and audited support access into a tenant. */
public interface PlatformService {

    CreateTenantResponse createTenant(CreateTenantRequest r);

    PageResponse<TenantDto> listTenants(int page, int size);

    TenantDto getTenant(UUID tenantId);

    TenantDto updateTenant(UUID tenantId, UpdateTenantRequest r);

    UserDetailDto createPlatformUser(CreatePlatformUserRequest r);

    List<UserSummaryDto> listPlatformUsers();

    PlatformUserProfileDto updatePlatformProfile(UUID userId, PlatformUserProfileDto d);

    PlatformSupportAccessDto requestSupportAccess(SupportAccessRequest r);

    /** A platform admin other than the requester approves (four-eyes rule). */
    PlatformSupportAccessDto approveSupportAccess(UUID id, boolean approve);

    /** Starts the session and returns a tenant-scoped token; every action is attributable through audit_log. */
    LoginResponse startSupportSession(UUID id);

    PlatformSupportAccessDto endSupportSession(UUID id);

    List<PlatformSupportAccessDto> mySupportAccess();
}
