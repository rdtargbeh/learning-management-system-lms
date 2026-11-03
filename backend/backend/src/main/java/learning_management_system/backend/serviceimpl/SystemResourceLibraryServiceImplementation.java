package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.SystemResourceDto;
import learning_management_system.backend.dto.SystemResourceRequest;
import learning_management_system.backend.entity.SystemResourceLibrary;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.SystemResourceLibraryResourceType;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.SystemResourceLibraryRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.SystemResourceLibraryService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class SystemResourceLibraryServiceImplementation implements SystemResourceLibraryService {
    private static final Set<UserType> CONTRIBUTORS = EnumSet.of(UserType.INSTRUCTOR, UserType.TEACHING_ASSISTANT, UserType.STAFF,
            UserType.ADVISOR, UserType.TENANT_ADMIN);

    private final SystemResourceLibraryRepository resources;
    private final UserRepository users;
    private final SystemTx sys;
    private final EntityRefresher refresher;

    public SystemResourceLibraryServiceImplementation(SystemResourceLibraryRepository resources, UserRepository users, SystemTx sys,
                                                      EntityRefresher refresher) {
        this.resources = resources;
        this.users = users;
        this.sys = sys;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<SystemResourceDto> list(SystemResourceLibraryResourceType type, String q) {
        List<SystemResourceLibrary> all = Access.ctx().platformAdmin() && Access.ctx().tenantId() == null
                ? resources.findByTenantIdIsNullOrderByResourceNameAsc() : resources.findAllByOrderByResourceNameAsc();
        String needle = q == null || q.isBlank() ? null : q.trim().toLowerCase(Locale.ROOT);
        return all.stream().filter(this::visible)
                .filter(x -> type == null || x.getResourceType() == type)
                .filter(x -> needle == null || x.getResourceName().toLowerCase(Locale.ROOT).contains(needle)
                        || (x.getResourceDescription() != null && x.getResourceDescription().toLowerCase(Locale.ROOT).contains(needle))
                        || (x.getTags() != null && x.getTags().toString().toLowerCase(Locale.ROOT).contains(needle)))
                .map(SystemResourceLibraryServiceImplementation::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public SystemResourceDto get(UUID resourceId) {
        return toDto(visibleOne(resourceId));
    }

    @Transactional
    @Override
    public SystemResourceDto create(SystemResourceRequest r) {
        if (r.resourceName() == null || r.resourceType() == null) throw ApiException.badRequest("NAME_AND_TYPE_REQUIRED", "resourceName and resourceType are required");
        boolean platform = Access.ctx().tenantId() == null;
        if (platform) Access.requirePlatformAdmin();
        else if (!CONTRIBUTORS.contains(me().getUserType())) throw ApiException.forbidden("Only staff can add resources");
        if (r.rating() != null && !manager(null)) throw ApiException.forbidden("Only library managers rate resources");
        validateTags(r);
        SystemResourceLibrary x = new SystemResourceLibrary();
        x.setTenantId(platform ? null : Access.ctx().tenantId());
        apply(x, r);
        x.setCreatorUserId(Access.ctx().userId());
        if (x.getIsPublic() == null) x.setIsPublic(false);
        x.setUsageCount(0);
        x.setDownloadCount(0);
        boolean autoApprove = manager(x);
        x.setIsApproved(autoApprove);
        if (autoApprove) { x.setDateApproved(OffsetDateTime.now()); x.setApprovedBy(Access.ctx().userId()); }
        return toDto(refresher.refresh(resources.saveAndFlush(x)));
    }

    @Transactional
    @Override
    public SystemResourceDto update(UUID resourceId, SystemResourceRequest r) {
        SystemResourceLibrary x = visibleOne(resourceId);
        boolean manager = manager(x);
        boolean creatorDraft = Access.isSelf(x.getCreatorUserId()) && !Boolean.TRUE.equals(x.getIsApproved());
        if (!manager && !creatorDraft) throw ApiException.forbidden("Only a library manager (or the creator, before approval) can change it");
        if (r.rating() != null && !manager) throw ApiException.forbidden("Only library managers rate resources");
        validateTags(r);
        apply(x, r);
        return toDto(resources.saveAndFlush(x));
    }

    @Transactional
    @Override
    public SystemResourceDto approve(UUID resourceId, boolean approved) {
        SystemResourceLibrary x = visibleOne(resourceId);
        if (!manager(x)) throw ApiException.forbidden("Only library managers approve resources");
        x.setIsApproved(approved);
        x.setDateApproved(approved ? OffsetDateTime.now() : null);
        x.setApprovedBy(approved ? Access.ctx().userId() : null);
        return toDto(resources.saveAndFlush(x));
    }

    @Transactional
    @Override
    public void delete(UUID resourceId) {
        SystemResourceLibrary x = visibleOne(resourceId);
        boolean creatorDraft = Access.isSelf(x.getCreatorUserId()) && !Boolean.TRUE.equals(x.getIsApproved());
        if (!manager(x) && !creatorDraft) throw ApiException.forbidden("Only a library manager (or the creator, before approval) can delete it");
        resources.delete(x);
    }

    @Transactional
    @Override
    public SystemResourceDto use(UUID resourceId) {
        SystemResourceLibrary x = approvedOne(resourceId);
        count(x, true);
        return toDto(reload(resourceId));
    }

    @Transactional
    @Override
    public SystemResourceDto download(UUID resourceId) {
        SystemResourceLibrary x = approvedOne(resourceId);
        if (x.getResourceUrl() == null) throw ApiException.conflict("NO_FILE", "The resource has nothing to download");
        count(x, false);
        return toDto(reload(resourceId));
    }

    // ---------------------------------------------------------------- rules

    /** Platform rows are written by platform admins only, so counters on them go through the trusted context. */
    private void count(SystemResourceLibrary x, boolean use) {
        if (x.getTenantId() == null && !Access.ctx().platformAdmin()) {
            sys.platformNew(() -> use ? resources.countUse(x.getResourceId()) : resources.countDownload(x.getResourceId()));
        } else if (use) resources.countUse(x.getResourceId());
        else resources.countDownload(x.getResourceId());
    }

    private boolean visible(SystemResourceLibrary x) {
        if (manager(x)) return true;
        if (x.getTenantId() == null) return Boolean.TRUE.equals(x.getIsApproved()) && Boolean.TRUE.equals(x.getIsPublic());
        if (Access.isSelf(x.getCreatorUserId())) return true;
        if (!Boolean.TRUE.equals(x.getIsApproved())) return false;
        return Boolean.TRUE.equals(x.getIsPublic()) || CONTRIBUTORS.contains(me().getUserType());
    }

    /** Platform rows: platform admins. Institution rows: COURSE_MANAGE of that institution. (x null: the caller's scope.) */
    private boolean manager(SystemResourceLibrary x) {
        boolean platformRow = x == null ? Access.ctx().tenantId() == null : x.getTenantId() == null;
        if (platformRow) return Access.ctx().platformAdmin() && Access.ctx().tenantId() == null;
        return Access.ctx().tenantId() != null && Access.hasTenantPermission(Permissions.COURSE_MANAGE);
    }

    private SystemResourceLibrary visibleOne(UUID id) {
        SystemResourceLibrary x = resources.findById(id).orElseThrow(() -> ApiException.notFound("Resource"));
        if (!visible(x)) throw ApiException.notFound("Resource");
        return x;
    }

    private SystemResourceLibrary approvedOne(UUID id) {
        SystemResourceLibrary x = visibleOne(id);
        if (!Boolean.TRUE.equals(x.getIsApproved())) throw ApiException.conflict("NOT_APPROVED", "The resource is waiting for approval");
        return x;
    }

    private SystemResourceLibrary reload(UUID id) {
        return resources.findById(id).orElseThrow();
    }

    private User me() {
        return users.findById(Access.ctx().userId()).orElseThrow(() -> ApiException.unauthorized("Sign in first"));
    }

    private static void apply(SystemResourceLibrary x, SystemResourceRequest r) {
        if (r.resourceName() != null) x.setResourceName(r.resourceName().trim());
        if (r.resourceType() != null) x.setResourceType(r.resourceType());
        if (r.resourceUrl() != null) x.setResourceUrl(r.resourceUrl());
        if (r.resourceDescription() != null) x.setResourceDescription(r.resourceDescription());
        if (r.isPublic() != null) x.setIsPublic(r.isPublic());
        if (r.rating() != null) x.setRating(r.rating());
        if (r.tags() != null) x.setTags(r.tags());
        if (r.metadata() != null) x.setMetadata(r.metadata());
    }

    private static void validateTags(SystemResourceRequest r) {
        if (r.tags() == null) return;
        if (!r.tags().isArray()) throw ApiException.badRequest("INVALID_TAGS", "tags must be a JSON array of strings");
        r.tags().forEach(t -> { if (!t.isTextual()) throw ApiException.badRequest("INVALID_TAGS", "tags must be a JSON array of strings"); });
    }

    private static SystemResourceDto toDto(SystemResourceLibrary x) {
        return new SystemResourceDto(x.getResourceId(), x.getTenantId() == null ? "PLATFORM" : "INSTITUTION", x.getResourceName(), x.getResourceType(),
                x.getResourceUrl(), x.getResourceDescription(), x.getCreatorUserId(), x.getIsPublic(), x.getIsApproved(), x.getDateApproved(),
                x.getApprovedBy(), x.getUsageCount(), x.getDownloadCount(), x.getRating(), x.getTags(), x.getMetadata(), x.getDateCreated());
    }
}
