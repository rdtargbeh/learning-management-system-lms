package learning_management_system.backend.config;

import learning_management_system.backend.dto.CreatePlatformUserRequest;
import learning_management_system.backend.entity.Permission;
import learning_management_system.backend.enums.Action;
import learning_management_system.backend.enums.PermissionScope;
import learning_management_system.backend.enums.PlatformRole;
import learning_management_system.backend.repository.PermissionRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.UserProvisioningService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/** Seeds the shared permission codes and, on an empty platform, the first super admin. Safe to run on every start. */
@Component
public class BootstrapRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BootstrapRunner.class);

    private final SystemTx sys;
    private final PermissionRepository permissions;
    private final UserRepository users;
    private final UserProvisioningService provisioning;
    private final String adminEmail;
    private final String adminPassword;

    public BootstrapRunner(SystemTx sys, PermissionRepository permissions, UserRepository users, UserProvisioningService provisioning,
                           @Value("${lms.bootstrap.platform-admin-email:}") String adminEmail,
                           @Value("${lms.bootstrap.platform-admin-password:}") String adminPassword) {
        this.sys = sys;
        this.permissions = permissions;
        this.users = users;
        this.provisioning = provisioning;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        sys.platform(() -> {
            Set<String> existing = permissions.findAll().stream().map(Permission::getPermissionCode).collect(Collectors.toSet());
            for (Permissions.Def d : Permissions.SYSTEM) {
                if (existing.contains(d.code())) continue;
                Permission p = new Permission();
                p.setTenantId(null);
                p.setPermissionCode(d.code());
                p.setPermissionName(d.name());
                p.setResourceType(d.resource());
                p.setAction(Action.valueOf(d.action()));
                p.setPermissionScope(PermissionScope.TENANT);
                permissions.save(p);
            }
            if (!adminEmail.isBlank() && !users.existsByIsSystemUserTrue()) {
                if (adminPassword.length() < 12) throw new IllegalStateException("LMS_BOOTSTRAP_ADMIN_PASSWORD must be at least 12 characters");
                String username = adminEmail.substring(0, adminEmail.indexOf('@') > 0 ? adminEmail.indexOf('@') : adminEmail.length());
                provisioning.provisionPlatformUser(new CreatePlatformUserRequest(username, adminEmail, adminPassword,
                        PlatformRole.SUPER_ADMIN, "Platform owner", null, true, null), null);
                log.warn("Created bootstrap platform super admin {} — change its password and set up MFA", adminEmail);
            }
            return null;
        });
    }
}
