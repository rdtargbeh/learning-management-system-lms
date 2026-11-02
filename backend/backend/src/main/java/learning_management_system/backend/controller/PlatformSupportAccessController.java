package learning_management_system.backend.controller;

import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.PlatformSupportAccessDto;
import learning_management_system.backend.dto.SupportAccessRequest;
import learning_management_system.backend.service.PlatformSupportAccessService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** platform_support_access: request, approve, start and end support sessions inside an institution. */
@RestController
@RequestMapping("/api/v1/platform/support-access")
public class PlatformSupportAccessController {
    private final PlatformSupportAccessService supportAccess;

    public PlatformSupportAccessController(PlatformSupportAccessService supportAccess) { this.supportAccess = supportAccess; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlatformSupportAccessDto request(@Valid @RequestBody SupportAccessRequest r) { return supportAccess.request(r); }

    @GetMapping
    public List<PlatformSupportAccessDto> list() { return supportAccess.list(); }

    @PostMapping("/{id}/approve")
    public PlatformSupportAccessDto approve(@PathVariable UUID id) { return supportAccess.decide(id, true); }

    @PostMapping("/{id}/deny")
    public PlatformSupportAccessDto deny(@PathVariable UUID id) { return supportAccess.decide(id, false); }

    /** Returns a tenant-scoped token for the approved session. */
    @PostMapping("/{id}/start")
    public LoginResponse start(@PathVariable UUID id) { return supportAccess.start(id); }

    @PostMapping("/{id}/end")
    public PlatformSupportAccessDto end(@PathVariable UUID id) { return supportAccess.end(id); }
}
