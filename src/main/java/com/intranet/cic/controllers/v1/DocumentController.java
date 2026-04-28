package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.DocumentDTO;
import com.intranet.cic.entities.Document;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.services.DocumentService;
import com.intranet.cic.services.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;           // ✅ not jakarta.annotation.Resource
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;                            // ✅ not jakarta.validation.Path

@RestController
@RequestMapping(path = "/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController extends AbstractController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<Page<Document>> getAllDocuments(
            @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        return sendOkResponse(documentService.getAllDocuments(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        return sendOkResponse(documentService.getDocumentById(id));
    }

    // ✅ multipart — file upload + document metadata
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> createDocument(
            @RequestPart("data") @Valid DocumentDTO documentDTO,
            @RequestPart("file") MultipartFile file
    ) {
        String fileUrl = fileStorageService.storeDocument(file);
        documentDTO.setFileUrl(fileUrl);
        return sendCreatedResponse(documentService.createDocument(documentDTO));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Document document = documentService.getDocumentById(id);

        if (!document.getAllowDownload()) {
            throw new IntranetException("Download not allowed for this document", HttpStatus.FORBIDDEN);
        }

        Path filePath = fileStorageService.resolveFilePath(document.getFileUrl()); // ✅ no cast needed

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new IntranetException("File not found", HttpStatus.NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            throw new IntranetException("File not found", HttpStatus.NOT_FOUND);
        }

        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // ✅ View document in browser
    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Document document = documentService.getDocumentById(id);

        if (!document.getAllowView()) {
            throw new IntranetException("View not allowed for this document", HttpStatus.FORBIDDEN);
        }

        Path filePath = fileStorageService.resolveFilePath(document.getFileUrl());

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new IntranetException("File not found", HttpStatus.NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            throw new IntranetException("File not found", HttpStatus.NOT_FOUND);
        }

        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")  // ✅ inline not attachment
                .body(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentDTO documentDTO
    ) {
        return sendOkResponse(documentService.updateDocument(id, documentDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        Document document = documentService.getDocumentById(id);
        fileStorageService.deleteFile(document.getFileUrl()); // ✅ delete physical file too
        documentService.deleteDocument(id);
        return sendNoContentResponse();
    }
}