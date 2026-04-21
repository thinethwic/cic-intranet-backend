package com.intranet.cic.controllers.v1;


import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.DocumentDTO;
import com.intranet.cic.entities.Document;
import com.intranet.cic.services.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController extends AbstractController {
    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<Page<Document>> getAllDocuments(
            @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        Page<Document> documents = documentService.getAllDocuments(pageable);
        return sendOkResponse(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        return sendOkResponse(documentService.getDocumentById(id));
    }

    @PostMapping
    public ResponseEntity<Document> createDocument(
            @Valid @RequestBody DocumentDTO documentDTO
    ) {
        Document document = documentService.createDocument(documentDTO);
        return sendCreatedResponse(document);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentDTO documentDTO
    ) {
        Document document = documentService.updateDocument(id, documentDTO);
        return sendOkResponse(document);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return sendNoContentResponse();
    }
}
