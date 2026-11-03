package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AddMasteryPathItemRequest;
import learning_management_system.backend.dto.MasteryPathItemDto;
import learning_management_system.backend.entity.MasteryPathRange;
import learning_management_system.backend.entity.MasteryPathRule;
import learning_management_system.backend.entity.MasteryPathSetItem;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.service.MasteryPathSetItemService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class MasteryPathSetItemServiceImplementation implements MasteryPathSetItemService {
    private final MasteryPathViews v;
    private final EntityRefresher refresher;

    public MasteryPathSetItemServiceImplementation(MasteryPathViews v, EntityRefresher refresher) {
        this.v = v;
        this.refresher = refresher;
    }

    @Transactional
    @Override
    public MasteryPathItemDto add(UUID rangeId, AddMasteryPathItemRequest r) {
        MasteryPathRange g = v.loadRange(rangeId);
        MasteryPathRule rule = v.loadRule(g.getRuleId());
        v.access.requireCourseEditor(rule.getCourseId());
        if ((r.contentId() == null) == (r.assignmentId() == null)) throw ApiException.badRequest("ONE_ITEM", "Give either contentId or assignmentId");
        if (r.assignmentId() != null) {
            if (r.assignmentId().equals(rule.getTriggerAssignmentId())) throw ApiException.badRequest("TRIGGER_ITEM", "The trigger assignment cannot be on its own path");
            v.assignments.findById(r.assignmentId()).filter(a -> a.getCourseId().equals(rule.getCourseId()))
                    .orElseThrow(() -> ApiException.badRequest("ITEM_NOT_IN_COURSE", "The assignment is not part of this course"));
        } else {
            v.contents.findById(r.contentId()).filter(c -> c.getCourseId().equals(rule.getCourseId()))
                    .orElseThrow(() -> ApiException.badRequest("ITEM_NOT_IN_COURSE", "The content item is not part of this course"));
        }
        boolean dup = v.items.findByRangeIdOrderBySetNumberAscDisplayOrderAsc(rangeId).stream()
                .anyMatch(i -> Objects.equals(i.getContentId(), r.contentId()) && Objects.equals(i.getAssignmentId(), r.assignmentId()));
        if (dup) throw ApiException.conflict("ITEM_EXISTS", "The item is already in this range");
        MasteryPathSetItem i = new MasteryPathSetItem();
        i.setRangeId(rangeId);
        i.setSetNumber(r.setNumber() == null ? 1 : r.setNumber());
        i.setContentId(r.contentId());
        i.setAssignmentId(r.assignmentId());
        i.setDisplayOrder(v.items.findByRangeIdOrderBySetNumberAscDisplayOrderAsc(rangeId).size());
        return v.item(refresher.refresh(v.items.saveAndFlush(i)));
    }

    @Transactional
    @Override
    public void delete(UUID setItemId) {
        MasteryPathSetItem i = v.items.findById(setItemId).orElseThrow(() -> ApiException.notFound("Path item"));
        v.access.requireCourseEditor(v.loadRule(v.loadRange(i.getRangeId()).getRuleId()).getCourseId());
        v.items.delete(i);
    }
}
