package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.ContentPermissionDto;
import learning_management_system.backend.dto.GrantContentPermissionRequest;
import learning_management_system.backend.entity.Content;
import learning_management_system.backend.entity.ContentPermission;
import learning_management_system.backend.entity.UserRole;
import learning_management_system.backend.enums.ContentAccessLevel;
import learning_management_system.backend.enums.PermissionType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.*;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.ContentPermissionService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContentPermissionServiceImplementation implements ContentPermissionService {
    /** What students may do with an item they can see, unless a denial says otherwise. */
    private static final Set<PermissionType> STUDENT_DEFAULTS = EnumSet.of(PermissionType.VIEW, PermissionType.DOWNLOAD,
            PermissionType.PRINT, PermissionType.COMMENT);

    private final ContentPermissionRepository permissions;
    private final ContentRepository contents;
    private final UserRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final CourseAccess access;
    private final EntityRefresher refresher;

    public ContentPermissionServiceImplementation(ContentPermissionRepository permissions, ContentRepository contents, UserRepository users,
                                                  RoleRepository roles, UserRoleRepository userRoles, CourseAccess access,
                                                  EntityRefresher refresher) {
        this.permissions = permissions;
        this.contents = contents;
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.access = access;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ContentPermissionDto> list(UUID contentId) {
        Content c = content(contentId);
        access.requireCourseEditor(c.getCourseId());
        return permissions.findByContentIdOrderByDateGrantedAsc(contentId).stream().map(ContentPermissionServiceImplementation::toDto).toList();
    }

    @Transactional
    @Override
    public ContentPermissionDto grant(UUID contentId, GrantContentPermissionRequest r) {
        Content c = content(contentId);
        access.requireCourseEditor(c.getCourseId());
        if ((r.userId() == null) == (r.roleId() == null)) throw ApiException.badRequest("ONE_TARGET", "Give either userId or roleId");
        if (r.userId() != null) users.findById(r.userId()).orElseThrow(() -> ApiException.badRequest("UNKNOWN_USER", "Unknown user"));
        if (r.roleId() != null) roles.findById(r.roleId()).orElseThrow(() -> ApiException.badRequest("UNKNOWN_ROLE", "Unknown role"));
        OffsetDateTime now = OffsetDateTime.now();
        if (r.dateExpires() != null && !r.dateExpires().isAfter(now)) throw ApiException.badRequest("INVALID_EXPIRY", "dateExpires must be in the future");
        // one row per target and permission: a new grant replaces the old one
        permissions.findByContentIdOrderByDateGrantedAsc(contentId).stream()
                .filter(p -> p.getPermissionType() == r.permissionType() && Objects.equals(p.getUserId(), r.userId()) && Objects.equals(p.getRoleId(), r.roleId()))
                .forEach(permissions::delete);
        permissions.flush();
        ContentPermission p = new ContentPermission();
        p.setContentId(contentId);
        p.setUserId(r.userId());
        p.setRoleId(r.roleId());
        p.setPermissionType(r.permissionType());
        p.setIsGranted(r.isGranted() == null || r.isGranted());
        p.setDateGranted(now);
        p.setDateExpires(r.dateExpires());
        p.setGrantedBy(Access.ctx().userId());
        return toDto(refresher.refresh(permissions.saveAndFlush(p)));
    }

    @Transactional
    @Override
    public void revoke(UUID contentId, UUID permissionId) {
        Content c = content(contentId);
        access.requireCourseEditor(c.getCourseId());
        permissions.delete(permissions.findByPermissionIdAndContentId(permissionId, contentId).orElseThrow(() -> ApiException.notFound("Permission")));
    }

    @Transactional(readOnly = true)
    @Override
    public Set<PermissionType> mine(UUID contentId) {
        Content c = content(contentId);
        Access.requireTenant();
        if (access.canSeeEverything() || access.isCourseStaff(c.getCourseId())) return EnumSet.allOf(PermissionType.class);
        return effective(c, Access.ctx().userId());
    }

    @Override
    public boolean canView(Content c, UUID userId) {
        return effective(c, userId).contains(PermissionType.VIEW);
    }

    /** Defaults for the access level, plus role and user grants, minus role and then user denials (the user's own rows win). */
    private Set<PermissionType> effective(Content c, UUID userId) {
        if (c.getAccessLevel() == ContentAccessLevel.INSTRUCTOR_ONLY) return EnumSet.noneOf(PermissionType.class);
        EnumSet<PermissionType> out = c.getAccessLevel() == ContentAccessLevel.RESTRICTED
                ? EnumSet.noneOf(PermissionType.class) : EnumSet.copyOf(STUDENT_DEFAULTS);
        List<ContentPermission> rows = permissions.findByContentIdOrderByDateGrantedAsc(c.getContentId()).stream().filter(this::active).toList();
        if (rows.isEmpty()) return out;
        Set<UUID> myRoles = userRoles.findByUserId(userId).stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        for (ContentPermission p : rows) if (p.getRoleId() != null && myRoles.contains(p.getRoleId())) apply(out, p);
        for (ContentPermission p : rows) if (userId.equals(p.getUserId())) apply(out, p);
        // without VIEW nothing else is possible
        if (!out.contains(PermissionType.VIEW)) out.clear();
        return out;
    }

    private static void apply(Set<PermissionType> out, ContentPermission p) {
        if (Boolean.TRUE.equals(p.getIsGranted())) out.add(p.getPermissionType());
        else out.remove(p.getPermissionType());
    }

    private boolean active(ContentPermission p) {
        return p.getDateExpires() == null || p.getDateExpires().isAfter(OffsetDateTime.now());
    }

    private static ContentPermissionDto toDto(ContentPermission p) {
        boolean active = p.getDateExpires() == null || p.getDateExpires().isAfter(OffsetDateTime.now());
        return new ContentPermissionDto(p.getPermissionId(), p.getContentId(), p.getUserId(), p.getRoleId(), p.getPermissionType(), p.getIsGranted(),
                p.getDateGranted(), p.getDateExpires(), p.getGrantedBy(), active);
    }

    private Content content(UUID id) {
        return contents.findById(id).orElseThrow(() -> ApiException.notFound("Content"));
    }
}
