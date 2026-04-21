package com.intranet.cic.services;

import com.intranet.cic.dtos.DocumentDTO;
import com.intranet.cic.entities.Document;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {
    Page<Document> getAllDocuments(Pageable pageable);
    Document getDocumentById (Long id);
    Document createDocument(DocumentDTO documentDTO);
    Document updateDocument(Long id, DocumentDTO documentDTO);
    void deleteDocument(Long id);

}
