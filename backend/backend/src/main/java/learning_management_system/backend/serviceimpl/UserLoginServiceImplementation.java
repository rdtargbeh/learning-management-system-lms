package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CreateUserLoginRequest;
import learning_management_system.backend.dto.UserLoginDto;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.entity.UserLogin;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.IdentityMapper;
import learning_management_system.backend.repository.UserLoginRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.AuditService;
import learning_management_system.backend.service.UserLoginService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserLoginServiceImplementation implements UserLoginService {
    private final UserLoginRepository logins;
    private final UserRepository users;
    private final AuditService audit;
    private final IdentityMapper identityMapper;
    private final EntityRefresher refresher;

    public UserLoginServiceImplementation(UserLoginRepository logins, UserRepository users, AuditService audit,
                                          IdentityMapper identityMapper, EntityRefresher refresher) {
        this.logins = logins;
        this.users = users;
        this.audit = audit;
        this.identityMapper = identityMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserLoginDto> list(UUID userId) {
        if (!Access.isSelf(userId)) Access.requireTenantPermission(Permissions.USER_READ);
        return logins.findByUserIdOrderByDateCreatedAsc(userId).stream().map(identityMapper::toDto).toList();
    }

    @Transactional
    @Override
    public UserLoginDto add(UUID userId, CreateUserLoginRequest r) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        User u = users.findById(userId).filter(x -> !Boolean.TRUE.equals(x.getIsSystemUser())).orElseThrow(() -> ApiException.notFound("User"));
        String name = r.uniqueLogin().trim();
        UUID tenantId = u.getTenantId();
        boolean taken = users.findByTenantIdAndUsernameIgnoreCase(tenantId, name).isPresent()
                || users.findByTenantIdAndEmail(tenantId, name).isPresent()
                || logins.findFirstByTenantIdAndUniqueLoginIgnoreCaseAndIsActiveTrue(tenantId, name).isPresent();
        if (taken) throw ApiException.conflict("LOGIN_TAKEN", "This sign-in name is already used in the institution");
        UserLogin l = new UserLogin();
        l.setUserId(userId);
        l.setUniqueLogin(name);
        l.setSisSourceId(r.sisSourceId());
        l.setIntegrationId(r.integrationId());
        l.setIsActive(true);
        refresher.refresh(logins.saveAndFlush(l));
        audit.record("LOGIN_ADDED", "USER", userId, userId, Severity.INFO, null, Map.of("uniqueLogin", name));
        return identityMapper.toDto(l);
    }

    @Transactional
    @Override
    public void remove(UUID userId, UUID loginId) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        UserLogin l = logins.findById(loginId).filter(x -> x.getUserId().equals(userId)).orElseThrow(() -> ApiException.notFound("Login"));
        logins.delete(l);
        logins.flush();
        audit.record("LOGIN_REMOVED", "USER", userId, userId, Severity.INFO, Map.of("uniqueLogin", l.getUniqueLogin()), null);
    }
}
