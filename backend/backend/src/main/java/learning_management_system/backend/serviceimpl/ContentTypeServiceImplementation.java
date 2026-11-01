package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.ContentTypeDto;
import learning_management_system.backend.dto.CreateContentTypeRequest;
import learning_management_system.backend.entity.ContentType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.ContentMapper;
import learning_management_system.backend.repository.ContentTypeRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.ContentTypeService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ContentTypeServiceImplementation implements ContentTypeService {
    /** Standard types: code -> display name. */
    static final Map<String, String> STANDARD = new LinkedHashMap<>();
    static {
        STANDARD.put("PAGE", "Page");
        STANDARD.put("VIDEO", "Video");
        STANDARD.put("FILE", "File");
        STANDARD.put("LINK", "External link");
        STANDARD.put("DOCUMENT", "Document");
        STANDARD.put("AUDIO", "Audio");
        STANDARD.put("INTERACTIVE", "Interactive content");
        STANDARD.put("EXTERNAL_TOOL", "External tool (LTI)");
        STANDARD.put("SCORM", "SCORM package");
    }

    private final ContentTypeRepository types;
    private final ContentMapper contentMapper;
    private final EntityRefresher refresher;

    public ContentTypeServiceImplementation(ContentTypeRepository types, ContentMapper contentMapper, EntityRefresher refresher) {
        this.types = types;
        this.contentMapper = contentMapper;
        this.refresher = refresher;
    }

    @Transactional
    @Override
    public List<ContentTypeDto> list() {
        Access.requireTenant();
        STANDARD.keySet().forEach(this::resolve);
        return types.findAllByOrderByTypeNameAsc().stream().map(contentMapper::toDto).toList();
    }

    @Transactional
    @Override
    public ContentTypeDto create(CreateContentTypeRequest r) {
        Access.requireTenantPermission(Permissions.COURSE_MANAGE);
        String code = r.typeCode().toUpperCase(Locale.ROOT);
        if (types.findByTypeCodeIgnoreCase(code).isPresent() || STANDARD.containsKey(code)) {
            throw ApiException.conflict("CONTENT_TYPE_EXISTS", "A content type with this code already exists");
        }
        ContentType t = contentMapper.toEntity(r);
        t.setTypeCode(code);
        t.setTypeName(r.typeName().trim());
        return contentMapper.toDto(refresher.refresh(types.saveAndFlush(t)));
    }

    @Transactional
    @Override
    public ContentType resolve(String typeCode) {
        String code = typeCode.trim().toUpperCase(Locale.ROOT);
        return types.findByTypeCodeIgnoreCase(code).orElseGet(() -> {
            String name = STANDARD.get(code);
            if (name == null) throw ApiException.badRequest("UNKNOWN_CONTENT_TYPE", "Unknown content type " + code);
            ContentType t = new ContentType();
            t.setTypeCode(code);
            t.setTypeName(name);
            return refresher.refresh(types.saveAndFlush(t));
        });
    }
}
