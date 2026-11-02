package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CommentLibraryItemDto;
import learning_management_system.backend.dto.CreateCommentLibraryItemRequest;
import learning_management_system.backend.entity.CommentLibraryItem;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.AssignmentMapper;
import learning_management_system.backend.repository.CommentLibraryItemRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.service.CommentLibraryService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommentLibraryServiceImplementation implements CommentLibraryService {
    private final CommentLibraryItemRepository items;
    private final AssignmentMapper assignmentMapper;
    private final EntityRefresher refresher;

    public CommentLibraryServiceImplementation(CommentLibraryItemRepository items, AssignmentMapper assignmentMapper,
                                               EntityRefresher refresher) {
        this.items = items;
        this.assignmentMapper = assignmentMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentLibraryItemDto> mine() {
        Access.requireTenant();
        return items.findByUserIdOrderByUsageCountDescDateCreatedDesc(Access.ctx().userId()).stream().map(assignmentMapper::toDto).toList();
    }

    @Transactional
    @Override
    public CommentLibraryItemDto add(CreateCommentLibraryItemRequest r) {
        Access.requireTenant();
        CommentLibraryItem i = new CommentLibraryItem();
        i.setUserId(Access.ctx().userId());
        i.setCommentText(r.commentText().trim());
        return assignmentMapper.toDto(refresher.refresh(items.saveAndFlush(i)));
    }

    @Transactional
    @Override
    public CommentLibraryItemDto use(UUID libraryItemId) {
        CommentLibraryItem i = own(libraryItemId);
        i.setUsageCount((i.getUsageCount() == null ? 0 : i.getUsageCount()) + 1);
        return assignmentMapper.toDto(items.saveAndFlush(i));
    }

    @Transactional
    @Override
    public void delete(UUID libraryItemId) {
        items.delete(own(libraryItemId));
        items.flush();
    }

    private CommentLibraryItem own(UUID id) {
        Access.requireTenant();
        return items.findById(id).filter(i -> Access.isSelf(i.getUserId())).orElseThrow(() -> ApiException.notFound("Comment"));
    }
}
