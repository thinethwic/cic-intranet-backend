package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.DocumentDTO;
import com.intranet.cic.entities.Announcement;
import com.intranet.cic.entities.Document;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.DocumentRepository;
import com.intranet.cic.services.DocumentService;
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
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
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
        try{
            Document document = modelMapper.map(documentDTO, Document.class);
            return documentRepository.save(document);
        } catch (Exception exception){
            log.error("Failed to create Announcement", exception);
            throw new IntranetException("Failed to create Announcement", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Document updateDocument(Long id, DocumentDTO documentDTO) {
        try{
            Document document = documentRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("Document Not found", HttpStatus.NOT_FOUND)
                    );

            modelMapper.map(documentDTO, Document.class);

            return documentRepository.save(document);
        }  catch (IntranetException intranetException) {

            log.warn("Document not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Document Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {

            log.error("Error updating Document", exception);
            throw new IntranetException("Failed to update Document", HttpStatus.INTERNAL_SERVER_ERROR);
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
}
