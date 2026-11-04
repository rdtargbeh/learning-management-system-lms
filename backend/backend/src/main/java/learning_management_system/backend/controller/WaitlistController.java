package learning_management_system.backend.controller;

import learning_management_system.backend.dto.EnrollmentDto;
import learning_management_system.backend.dto.JoinWaitlistRequest;
import learning_management_system.backend.dto.WaitlistDto;
import learning_management_system.backend.service.WaitlistService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Section waitlists. */
@RestController
@RequestMapping("/api/v1")
public class WaitlistController {
    private final WaitlistService waitlist;

    public WaitlistController(WaitlistService waitlist) { this.waitlist = waitlist; }

    @GetMapping("/sections/{sectionId}/waitlist")
    public List<WaitlistDto> list(@PathVariable UUID sectionId) { return waitlist.list(sectionId); }

    @PostMapping("/sections/{sectionId}/waitlist")
    @ResponseStatus(HttpStatus.CREATED)
    public WaitlistDto join(@PathVariable UUID sectionId, @RequestBody(required = false) JoinWaitlistRequest r) {
        return waitlist.join(sectionId, r);
    }

    @GetMapping("/waitlist/mine")
    public List<WaitlistDto> mine() { return waitlist.mine(); }

    @PostMapping("/waitlist/{waitlistId}/leave")
    public WaitlistDto leave(@PathVariable UUID waitlistId) { return waitlist.leave(waitlistId); }

    @PostMapping("/waitlist/{waitlistId}/accept")
    public EnrollmentDto accept(@PathVariable UUID waitlistId) { return waitlist.accept(waitlistId); }

    @PostMapping("/waitlist/{waitlistId}/decline")
    public WaitlistDto decline(@PathVariable UUID waitlistId) { return waitlist.decline(waitlistId); }
}
