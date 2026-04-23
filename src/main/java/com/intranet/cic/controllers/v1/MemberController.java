package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.MemberDTO;
import com.intranet.cic.entities.Member;
import com.intranet.cic.services.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController extends AbstractController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<Page<Member>> getAllMembers(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        log.info("Request to get all members");
        Page<Member> members = memberService.getAllMembers(pageable);
        return sendOkResponse(members);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        log.info("Request to get member with id: {}", id);
        Member member = memberService.getMemberById(id);
        return sendOkResponse(member);
    }

    @PostMapping
    public ResponseEntity<Member> createMember(@RequestBody MemberDTO memberDTO) {
        log.info("Request to create member");
        Member created = memberService.createMember(memberDTO);
        return sendCreatedResponse(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(
            @PathVariable Long id,
            @RequestBody MemberDTO memberDTO) {
        log.info("Request to update member with id: {}", id);
        Member updated = memberService.updateMember(id, memberDTO);
        return sendOkResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        log.info("Request to delete member with id: {}", id);
        memberService.deleteMember(id);
        return sendNoContentResponse();
    }
}
