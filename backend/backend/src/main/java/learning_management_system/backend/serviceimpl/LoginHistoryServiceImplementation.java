package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.LoginHistoryDto;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.entity.LoginHistory;
import learning_management_system.backend.enums.LoginMethod;
import learning_management_system.backend.enums.LoginStatus;
import learning_management_system.backend.mapper.IdentityMapper;
import learning_management_system.backend.repository.LoginHistoryRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.LoginHistoryService;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.util.UUID;

@Service
public class LoginHistoryServiceImplementation implements LoginHistoryService {
    private final LoginHistoryRepository loginHistory;
    private final IdentityMapper identityMapper;
    private final SystemTx sys;

    public LoginHistoryServiceImplementation(LoginHistoryRepository loginHistory, IdentityMapper identityMapper, SystemTx sys) {
        this.loginHistory = loginHistory;
        this.identityMapper = identityMapper;
        this.sys = sys;
    }

    @Override
    public void record(UUID userId, UUID tenantId, String login, InetAddress ip, LoginStatus status, String failureReason, boolean mfaUsed) {
        LoginHistory h = new LoginHistory();
        h.setUserId(userId);
        h.setTenantId(tenantId);
        String name = login == null ? "" : login;
        h.setUsername(name.length() > 100 ? name.substring(0, 100) : name);
        h.setIpAddress(ip);
        h.setLoginStatus(status);
        h.setLoginMethod(LoginMethod.PASSWORD);
        h.setFailureReason(failureReason);
        h.setMfaUsed(mfaUsed);
        if (tenantId == null) sys.platform(() -> loginHistory.save(h));
        else sys.inTenant(tenantId, () -> loginHistory.save(h));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<LoginHistoryDto> list(UUID userId, int page, int size) {
        if (!Access.isSelf(userId)) Access.requireTenantPermission(Permissions.USER_READ);
        return PageResponse.of(loginHistory.findByUserIdOrderByLoginTimestampDesc(userId, PageRequest.of(page, Math.min(size, 200))),
                identityMapper::toDto);
    }
}
