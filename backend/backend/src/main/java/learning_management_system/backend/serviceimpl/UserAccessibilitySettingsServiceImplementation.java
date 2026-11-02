package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AccessibilitySettingsDto;
import learning_management_system.backend.entity.UserAccessibilitySettings;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.IdentityMapper;
import learning_management_system.backend.repository.UserAccessibilitySettingsRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.UserAccessibilitySettingsService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class UserAccessibilitySettingsServiceImplementation implements UserAccessibilitySettingsService {
    private static final Set<Integer> EXTENDED_TIME = Set.of(100, 125, 150, 175, 200, 250);

    private final UserAccessibilitySettingsRepository accessibility;
    private final UserRepository users;
    private final IdentityMapper identityMapper;
    private final EntityRefresher refresher;

    public UserAccessibilitySettingsServiceImplementation(UserAccessibilitySettingsRepository accessibility, UserRepository users,
                                                          IdentityMapper identityMapper, EntityRefresher refresher) {
        this.accessibility = accessibility;
        this.users = users;
        this.identityMapper = identityMapper;
        this.refresher = refresher;
    }

    @Transactional
    @Override
    public AccessibilitySettingsDto get(UUID userId) {
        UUID id = userId == null ? me() : userId;
        if (!Access.isSelf(id)) Access.requireTenantPermission(Permissions.USER_READ);
        users.findById(id).orElseThrow(() -> ApiException.notFound("User"));
        return identityMapper.toDto(row(id));
    }

    @Transactional
    @Override
    public AccessibilitySettingsDto update(AccessibilitySettingsDto d) {
        if (d.extendedTimePercentage() != null && !EXTENDED_TIME.contains(d.extendedTimePercentage())) {
            throw ApiException.badRequest("INVALID_EXTENDED_TIME", "extendedTimePercentage must be 100, 125, 150, 175, 200 or 250");
        }
        if (Boolean.TRUE.equals(d.colorBlindMode()) && (d.colorBlindType() == null || d.colorBlindType().isBlank())) {
            throw ApiException.badRequest("COLOR_BLIND_TYPE_REQUIRED", "colorBlindType is required when colorBlindMode is on");
        }
        UserAccessibilitySettings s = row(me());
        identityMapper.updateEntity(d, s);
        accessibility.saveAndFlush(s);
        return identityMapper.toDto(refresher.refresh(s));
    }

    /** Rows are created on first use with the database defaults. */
    private UserAccessibilitySettings row(UUID userId) {
        return accessibility.findByUserId(userId).orElseGet(() -> {
            UserAccessibilitySettings s = new UserAccessibilitySettings();
            s.setUserId(userId);
            return refresher.refresh(accessibility.saveAndFlush(s));
        });
    }

    private static UUID me() {
        UUID id = Access.ctx().userId();
        if (id == null) throw ApiException.unauthorized("Sign in first");
        return id;
    }
}
