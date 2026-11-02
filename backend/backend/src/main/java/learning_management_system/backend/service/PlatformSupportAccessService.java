package learning_management_system.backend.service;

import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.PlatformSupportAccessDto;
import learning_management_system.backend.dto.SupportAccessRequest;

import java.util.List;
import java.util.UUID;

/** platform_support_access: audited, approved, time-boxed sessions of platform staff inside an institution. */
public interface PlatformSupportAccessService {

    PlatformSupportAccessDto request(SupportAccessRequest r);

    /** A platform admin other than the requester approves or denies (four-eyes rule). */
    PlatformSupportAccessDto decide(UUID id, boolean approve);

    /** Starts the session and returns a tenant-scoped token; every action is attributable through audit_log. */
    LoginResponse start(UUID id);

    PlatformSupportAccessDto end(UUID id);

    /** The caller's own requests; platform admins see all. */
    List<PlatformSupportAccessDto> list();
}
