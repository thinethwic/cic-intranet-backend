package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.MemberDTO;
import com.intranet.cic.entities.Member;
import com.intranet.cic.entities.User;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.MemberRepository;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    @Override
    public Page<Member> getAllMembers(Pageable pageable) {
        try{
            return memberRepository.findAll(pageable);
        } catch (Exception exception){
            log.error("Failed to get all members", exception);
            throw new IntranetException("Failed to get all members", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Member getMemberById(Long id) {
        try{
            return memberRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("Member Not found", HttpStatus.NOT_FOUND)
                    );
        } catch (IntranetException intranetException) {

            log.warn("Member not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Member Not found", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            log.error("Error getting member", exception);
            throw new IntranetException("Failed to get member", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Member createMember(MemberDTO memberDTO) {
        try {
            Member member = modelMapper.map(memberDTO, Member.class);


            if (memberDTO.getUserId() != null) {
                User user = userRepository.findById(memberDTO.getUserId())
                        .orElseThrow(() -> new IntranetException("User not found", HttpStatus.NOT_FOUND));
                member.setUser(user);
            }

            return memberRepository.save(member);
        } catch (IntranetException intranetException) {
            log.warn("User not found with id: {} to create member", memberDTO.getUserId(), intranetException);
            throw intranetException;
        } catch (Exception exception) {
            log.error("Failed to create member", exception);
            throw new IntranetException("Failed to create member", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Member updateMember(Long id, MemberDTO memberDTO) {
        try {
            Member member = memberRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Member not found", HttpStatus.NOT_FOUND));

            // Save user reference before ModelMapper overwrites it
            User existingUser = member.getUser();

            modelMapper.map(memberDTO, member);   // map first

            // ✅ Re-fetch and set user if new userId provided, otherwise retain existing
            if (memberDTO.getUserId() != null) {
                User user = userRepository.findById(memberDTO.getUserId())
                        .orElseThrow(() -> new IntranetException("User not found", HttpStatus.NOT_FOUND));
                member.setUser(user);
            } else {
                member.setUser(existingUser);     // ✅ retain existing user after mapping
            }

            return memberRepository.save(member);
        } catch (IntranetException intranetException) {
            log.warn("Member or User not found for id: {}", id, intranetException);
            throw intranetException;
        } catch (Exception exception) {
            log.error("Error updating member", exception);
            throw new IntranetException("Failed to update member", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteMember(Long id) {
        try{
            Member member = memberRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("Member Not found", HttpStatus.NOT_FOUND)
                    );

            memberRepository.delete(member);
        }  catch (IntranetException intranetException) {

            log.warn("Member not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Member Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {

            log.error("Error updating Member", exception);
            throw new IntranetException("Failed to update Member", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
