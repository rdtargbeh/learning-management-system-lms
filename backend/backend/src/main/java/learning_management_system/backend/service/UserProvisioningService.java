package learning_management_system.backend.service;

import learning_management_system.backend.dto.CreatePlatformUserRequest;
import learning_management_system.backend.dto.CreateUserRequest;
import learning_management_system.backend.entity.User;

import java.util.UUID;

/**
 * The single code path that creates accounts in {@code users}. Callers must already be authorized and inside a
 * transaction; the database trigger then creates user_profile and the profile for the user_type.
 */
public interface UserProvisioningService {

    User provisionTenantUser(CreateUserRequest r, UUID tenantId);

    User provisionPlatformUser(CreatePlatformUserRequest r, UUID createdBy);
}
