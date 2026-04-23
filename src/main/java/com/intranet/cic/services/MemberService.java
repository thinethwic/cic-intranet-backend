package com.intranet.cic.services;

import com.intranet.cic.dtos.MemberDTO;
import com.intranet.cic.entities.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {
    Page<Member> getAllMembers(Pageable pageable);
    Member getMemberById(Long id);
    Member createMember(MemberDTO memberDTO);
    Member updateMember(Long id, MemberDTO memberDTO);
    void deleteMember(Long id);
}
