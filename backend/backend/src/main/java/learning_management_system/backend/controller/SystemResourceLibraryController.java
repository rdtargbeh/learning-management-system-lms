package learning_management_system.backend.controller;

import learning_management_system.backend.dto.SystemResourceDto;
import learning_management_system.backend.dto.SystemResourceRequest;
import learning_management_system.backend.enums.SystemResourceLibraryResourceType;
import learning_management_system.backend.service.SystemResourceLibraryService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** system_resource_library: the shared resource library (platform-wide and institution resources). */
@RestController
@RequestMapping("/api/v1/resource-library")
public class SystemResourceLibraryController {
    private final SystemResourceLibraryService resources;

    public SystemResourceLibraryController(SystemResourceLibraryService resources) { this.resources = resources; }

    @GetMapping
    public List<SystemResourceDto> list(@RequestParam(required = false) SystemResourceLibraryResourceType type,
                                        @RequestParam(required = false) String q) {
        return resources.list(type, q);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SystemResourceDto create(@Valid @RequestBody SystemResourceRequest r) { return resources.create(r); }

    @GetMapping("/{resourceId}")
    public SystemResourceDto get(@PathVariable UUID resourceId) { return resources.get(resourceId); }

    @PatchMapping("/{resourceId}")
    public SystemResourceDto update(@PathVariable UUID resourceId, @Valid @RequestBody SystemResourceRequest r) { return resources.update(resourceId, r); }

    @PostMapping("/{resourceId}/approve")
    public SystemResourceDto approve(@PathVariable UUID resourceId) { return resources.approve(resourceId, true); }

    @PostMapping("/{resourceId}/unapprove")
    public SystemResourceDto unapprove(@PathVariable UUID resourceId) { return resources.approve(resourceId, false); }

    @PostMapping("/{resourceId}/use")
    public SystemResourceDto use(@PathVariable UUID resourceId) { return resources.use(resourceId); }

    @PostMapping("/{resourceId}/download")
    public SystemResourceDto download(@PathVariable UUID resourceId) { return resources.download(resourceId); }

    @DeleteMapping("/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID resourceId) { resources.delete(resourceId); }
}
