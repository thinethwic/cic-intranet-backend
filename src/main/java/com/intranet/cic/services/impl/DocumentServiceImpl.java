package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.DocumentDTO;
import com.intranet.cic.entities.Document;
import com.intranet.cic.entities.Member;
import com.intranet.cic.entities.User;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.DocumentRepository;
import com.intranet.cic.repositories.MemberRepository;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<Document> getAllDocuments(Pageable pageable) {
        try{
            return documentRepository.findAll(pageable);
        } catch (Exception exception){
            log.error("Failed to get all documents", exception);
            throw new IntranetException("Failed to get all documents", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Document getDocumentById(Long id) {
        try{
            return documentRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("Document Not found", HttpStatus.NOT_FOUND)
                    );
        } catch (IntranetException intranetException) {

            log.warn("Document not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Document Not found", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            log.error("Error getting document", exception);
            throw new IntranetException("Failed to get document", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Document createDocument(DocumentDTO documentDTO) {
        try {
            User createdBy = userRepository.findById(documentDTO.getCreatedById())
                    .orElseThrow(() -> new IntranetException("User not found", HttpStatus.NOT_FOUND));

            List<Member> members = resolveMembers(documentDTO.getMemberIds());

            Document document = modelMapper.map(documentDTO, Document.class);
            document.setCreatedBy(createdBy);
            document.setMembers(members);

            return documentRepository.save(document);
        } catch (IntranetException intranetException) {
            log.warn("User or Member not found while creating document", intranetException);
            throw intranetException;
        } catch (Exception exception) {
            log.error("Failed to create document", exception);
            throw new IntranetException("Failed to create document", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Document updateDocument(Long id, DocumentDTO documentDTO) {
        try {
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Document not found", HttpStatus.NOT_FOUND));

            // ✅ Save relations before modelMapper wipes them
            User existingCreatedBy = document.getCreatedBy();
            List<Member> existingMembers = document.getMembers();
            String existingFileUrl = document.getFileUrl();

            modelMapper.map(documentDTO, document);

            // ✅ Restore fileUrl — never changed on metadata update
            document.setFileUrl(existingFileUrl);

            // ✅ Update createdBy only if explicitly provided
            if (documentDTO.getCreatedById() != null) {
                User createdBy = userRepository.findById(documentDTO.getCreatedById())
                        .orElseThrow(() -> new IntranetException("User not found", HttpStatus.NOT_FOUND));
                document.setCreatedBy(createdBy);
            } else {
                document.setCreatedBy(existingCreatedBy);
            }

            // ✅ Update members only if explicitly provided
            if (documentDTO.getMemberIds() != null && !documentDTO.getMemberIds().isEmpty()) {
                document.setMembers(resolveMembers(documentDTO.getMemberIds()));
            } else {
                document.setMembers(existingMembers);
            }

            return documentRepository.save(document);

        } catch (IntranetException e) {
            log.warn("Document, User or Member not found for id: {}", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Error updating document id: {}", id, e);
            throw new IntranetException("Failed to update document", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteDocument(Long id) {
        try{
            Document document = documentRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("Document Not found", HttpStatus.NOT_FOUND)
                    );

            documentRepository.delete(document);
        }  catch (IntranetException intranetException) {

            log.warn("Document not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Document Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {

            log.error("Error updating Document", exception);
            throw new IntranetException("Failed to update Document", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private List<Member> resolveMembers(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) return new ArrayList<>();

        List<Member> members = memberRepository.findAllById(memberIds);

        if (members.size() != memberIds.size()) {
            throw new IntranetException("One or more members not found", HttpStatus.NOT_FOUND);
        }

        return members;
    }
}
