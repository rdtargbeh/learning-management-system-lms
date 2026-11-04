package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AddTagMembersRequest;
import learning_management_system.backend.dto.DifferentiationTagMemberDto;
import learning_management_system.backend.service.DifferentiationTagMemberService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** differentiation_tag_member: the students carrying a tag. */
@RestController
@RequestMapping("/api/v1/tags/{tagId}/members")
public class DifferentiationTagMemberController {
    private final DifferentiationTagMemberService tagMembers;

    public DifferentiationTagMemberController(DifferentiationTagMemberService tagMembers) { this.tagMembers = tagMembers; }

    @GetMapping
    public List<DifferentiationTagMemberDto> list(@PathVariable UUID tagId) { return tagMembers.list(tagId); }

    @PostMapping
    public List<DifferentiationTagMemberDto> add(@PathVariable UUID tagId, @Valid @RequestBody AddTagMembersRequest r) { return tagMembers.add(tagId, r); }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID tagId, @PathVariable UUID userId) { tagMembers.remove(tagId, userId); }
}
