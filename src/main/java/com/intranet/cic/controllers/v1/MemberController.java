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
public class MemberController extends AbstractController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<Page<Member>> getAllMembers(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<Member> members = memberService.getAllMembers(pageable);
        return sendOkResponse(members);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        return sendOkResponse(member);
    }

    @PostMapping
    public ResponseEntity<Member> createMember(@RequestBody MemberDTO memberDTO) {
        Member created = memberService.createMember(memberDTO);
        return sendCreatedResponse(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(
            @PathVariable Long id,
            @RequestBody MemberDTO memberDTO) {
        Member updated = memberService.updateMember(id, memberDTO);
        return sendOkResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return sendNoContentResponse();
    }
}
