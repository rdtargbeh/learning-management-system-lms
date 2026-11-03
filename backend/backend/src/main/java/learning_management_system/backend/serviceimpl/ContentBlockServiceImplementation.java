package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.ContentBlockDto;
import learning_management_system.backend.dto.ContentBlockRequest;
import learning_management_system.backend.dto.ContentDto;
import learning_management_system.backend.dto.ReorderRequest;
import learning_management_system.backend.entity.Content;
import learning_management_system.backend.entity.ContentBlock;
import learning_management_system.backend.enums.BlueprintContentLockObjectType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.ContentBlockMapper;
import learning_management_system.backend.repository.ContentBlockRepository;
import learning_management_system.backend.repository.ContentRepository;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.BlueprintContentLockService;
import learning_management_system.backend.service.ContentBlockService;
import learning_management_system.backend.service.ContentService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static learning_management_system.backend.enums.BlueprintLockAspect.CONTENT;

@Service
public class ContentBlockServiceImplementation implements ContentBlockService {
    private final ContentBlockRepository blocks;
    private final ContentRepository contents;
    private final ContentService contentService;
    private final BlueprintContentLockService blueprintLocks;
    private final CourseAccess access;
    private final ContentBlockMapper mapper;
    private final EntityRefresher refresher;

    public ContentBlockServiceImplementation(ContentBlockRepository blocks, ContentRepository contents, ContentService contentService,
                                             BlueprintContentLockService blueprintLocks, CourseAccess access, ContentBlockMapper mapper,
                                             EntityRefresher refresher) {
        this.blocks = blocks;
        this.contents = contents;
        this.contentService = contentService;
        this.blueprintLocks = blueprintLocks;
        this.access = access;
        this.mapper = mapper;
        this.refresher = refresher;
    }

    @Transactional
    @Override
    public List<ContentBlockDto> list(UUID contentId) {
        ContentDto view = contentService.get(contentId);              // visibility and locks for students
        if (view.lockReason() != null) throw ApiException.conflict("CONTENT_LOCKED", view.lockReason());
        return blocks.findByContentIdOrderByDisplayOrderAscBlockNumberAsc(contentId).stream().map(mapper::toDto).toList();
    }

    @Transactional
    @Override
    public ContentBlockDto add(UUID contentId, ContentBlockRequest r) {
        Content x = editable(contentId);
        if (r.blockType() == null) throw ApiException.badRequest("BLOCK_TYPE_REQUIRED", "blockType is required");
        ContentBlock b = new ContentBlock();
        b.setContentId(x.getContentId());
        mapper.updateEntity(r, b);
        int n = blocks.maxBlockNumber(contentId) + 1;
        b.setBlockNumber(n);
        b.setDisplayOrder(n);
        return mapper.toDto(refresher.refresh(blocks.saveAndFlush(b)));
    }

    @Transactional
    @Override
    public ContentBlockDto update(UUID blockId, ContentBlockRequest r) {
        ContentBlock b = block(blockId);
        editable(b.getContentId());
        mapper.updateEntity(r, b);
        blocks.saveAndFlush(b);
        return mapper.toDto(refresher.refresh(b));
    }

    @Transactional
    @Override
    public void delete(UUID blockId) {
        ContentBlock b = block(blockId);
        editable(b.getContentId());
        blocks.delete(b);
    }

    @Transactional
    @Override
    public List<ContentBlockDto> reorder(UUID contentId, ReorderRequest r) {
        editable(contentId);
        List<ContentBlock> all = blocks.findByContentIdOrderByDisplayOrderAscBlockNumberAsc(contentId);
        Map<UUID, ContentBlock> byId = new HashMap<>();
        all.forEach(b -> byId.put(b.getBlockId(), b));
        if (new HashSet<>(r.orderedIds()).size() != r.orderedIds().size() || !byId.keySet().equals(new HashSet<>(r.orderedIds()))) {
            throw ApiException.badRequest("ORDER_MISMATCH", "orderedIds must list every block of the page once");
        }
        for (int i = 0; i < r.orderedIds().size(); i++) byId.get(r.orderedIds().get(i)).setDisplayOrder(i + 1);
        blocks.saveAllAndFlush(all);
        return blocks.findByContentIdOrderByDisplayOrderAscBlockNumberAsc(contentId).stream().map(mapper::toDto).toList();
    }

    private Content editable(UUID contentId) {
        Content x = contents.findById(contentId).orElseThrow(() -> ApiException.notFound("Content"));
        access.requireCourseEditor(x.getCourseId());
        blueprintLocks.requireEditable(x.getCourseId(), BlueprintContentLockObjectType.CONTENT, contentId, EnumSet.of(CONTENT));
        return x;
    }

    private ContentBlock block(UUID id) {
        return blocks.findById(id).orElseThrow(() -> ApiException.notFound("Block"));
    }
}
