package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.ContentDto;
import learning_management_system.backend.dto.InteractiveContentDto;
import learning_management_system.backend.entity.Content;
import learning_management_system.backend.entity.InteractiveContent;
import learning_management_system.backend.enums.BlueprintContentLockObjectType;
import learning_management_system.backend.enums.LaunchMethod;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.ContentBlockMapper;
import learning_management_system.backend.repository.ContentRepository;
import learning_management_system.backend.repository.ContentTypeRepository;
import learning_management_system.backend.repository.InteractiveContentRepository;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.BlueprintContentLockService;
import learning_management_system.backend.service.ContentService;
import learning_management_system.backend.service.InteractiveContentService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static learning_management_system.backend.enums.BlueprintLockAspect.CONTENT;

@Service
public class InteractiveContentServiceImplementation implements InteractiveContentService {
    private static final Set<String> TYPES = Set.of("INTERACTIVE", "SCORM", "EXTERNAL_TOOL");
    private static final Set<LaunchMethod> NEED_URL = EnumSet.of(LaunchMethod.EMBED, LaunchMethod.IFRAME, LaunchMethod.NEW_WINDOW, LaunchMethod.XAPI);

    private final InteractiveContentRepository interactive;
    private final ContentRepository contents;
    private final ContentTypeRepository types;
    private final ContentService contentService;
    private final BlueprintContentLockService blueprintLocks;
    private final CourseAccess access;
    private final ContentBlockMapper mapper;
    private final EntityRefresher refresher;

    public InteractiveContentServiceImplementation(InteractiveContentRepository interactive, ContentRepository contents,
                                                   ContentTypeRepository types, ContentService contentService,
                                                   BlueprintContentLockService blueprintLocks, CourseAccess access,
                                                   ContentBlockMapper mapper, EntityRefresher refresher) {
        this.interactive = interactive;
        this.contents = contents;
        this.types = types;
        this.contentService = contentService;
        this.blueprintLocks = blueprintLocks;
        this.access = access;
        this.mapper = mapper;
        this.refresher = refresher;
    }

    @Transactional
    @Override
    public InteractiveContentDto get(UUID contentId) {
        ContentDto view = contentService.get(contentId);
        if (view.lockReason() != null) throw ApiException.conflict("CONTENT_LOCKED", view.lockReason());
        return interactive.findByContentId(contentId).map(mapper::toDto).orElseThrow(() -> ApiException.notFound("Launch details"));
    }

    @Transactional
    @Override
    public InteractiveContentDto replace(UUID contentId, InteractiveContentDto d) {
        Content x = editable(contentId);
        String type = types.findById(x.getTypeId()).map(t -> t.getTypeCode().toUpperCase()).orElse("");
        if (!TYPES.contains(type)) {
            throw ApiException.badRequest("NOT_INTERACTIVE", "Launch details are for INTERACTIVE, SCORM and EXTERNAL_TOOL items");
        }
        if (d.launchMethod() == null) throw ApiException.badRequest("LAUNCH_METHOD_REQUIRED", "launchMethod is required");
        if (d.launchMethod() == LaunchMethod.LTI && blank(d.ltiToolId())) throw ApiException.badRequest("LTI_TOOL_REQUIRED", "LTI launches need ltiToolId");
        if (d.launchMethod() == LaunchMethod.SCORM && blank(d.scormPackageId())) {
            throw ApiException.badRequest("SCORM_PACKAGE_REQUIRED", "SCORM launches need scormPackageId");
        }
        if (NEED_URL.contains(d.launchMethod()) && (blank(d.interactionUrl()) || !d.interactionUrl().startsWith("https://"))) {
            throw ApiException.badRequest("HTTPS_URL_REQUIRED", d.launchMethod() + " launches need an https interactionUrl");
        }
        if (Boolean.FALSE.equals(d.allowsMultipleAttempts()) && d.maxAttempts() != null && d.maxAttempts() > 1) {
            throw ApiException.badRequest("ATTEMPTS_CONFLICT", "maxAttempts above 1 needs allowsMultipleAttempts");
        }
        InteractiveContent i = interactive.findByContentId(contentId).orElseGet(() -> {
            InteractiveContent n = new InteractiveContent();
            n.setContentId(contentId);
            return n;
        });
        mapper.replaceEntity(d, i);
        if (i.getAllowsMultipleAttempts() == null) i.setAllowsMultipleAttempts(true);
        return mapper.toDto(refresher.refresh(interactive.saveAndFlush(i)));
    }

    @Transactional
    @Override
    public void delete(UUID contentId) {
        editable(contentId);
        interactive.delete(interactive.findByContentId(contentId).orElseThrow(() -> ApiException.notFound("Launch details")));
    }

    private Content editable(UUID contentId) {
        Content x = contents.findById(contentId).orElseThrow(() -> ApiException.notFound("Content"));
        access.requireCourseEditor(x.getCourseId());
        blueprintLocks.requireEditable(x.getCourseId(), BlueprintContentLockObjectType.CONTENT, contentId, EnumSet.of(CONTENT));
        return x;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
