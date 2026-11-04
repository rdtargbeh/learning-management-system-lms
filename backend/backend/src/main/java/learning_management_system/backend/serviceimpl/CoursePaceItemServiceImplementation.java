package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CoursePaceItemDto;
import learning_management_system.backend.dto.UpdatePaceItemRequest;
import learning_management_system.backend.entity.CoursePace;
import learning_management_system.backend.entity.CoursePaceItem;
import learning_management_system.backend.enums.PaceStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.CoursePaceItemRepository;
import learning_management_system.backend.repository.CoursePaceRepository;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.CoursePaceItemService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CoursePaceItemServiceImplementation implements CoursePaceItemService {
    private final CoursePaceItemRepository paceItems;
    private final CoursePaceRepository paces;
    private final CoursePaceServiceImplementation paceService;
    private final CourseAccess access;

    public CoursePaceItemServiceImplementation(CoursePaceItemRepository paceItems, CoursePaceRepository paces,
                                               CoursePaceServiceImplementation paceService, CourseAccess access) {
        this.paceItems = paceItems;
        this.paces = paces;
        this.paceService = paceService;
        this.access = access;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CoursePaceItemDto> list(UUID paceId) {
        return paceService.get(paceId).items();
    }

    /** Durations change the dates at once, also for a published pace. */
    @Transactional
    @Override
    public CoursePaceItemDto update(UUID paceItemId, UpdatePaceItemRequest r) {
        CoursePaceItem i = paceItems.findById(paceItemId).orElseThrow(() -> ApiException.notFound("Pace item"));
        CoursePace p = paces.findById(i.getPaceId()).orElseThrow();
        access.requireCourseEditor(p.getCourseId());
        if (p.getPaceStatus() == PaceStatus.ARCHIVED) throw ApiException.conflict("ARCHIVED", "The pace is archived");
        i.setDurationDays(r.durationDays());
        return paceService.itemDto(paceItems.saveAndFlush(i));
    }
}
