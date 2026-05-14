package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.DocumentDTO;
import com.intranet.cic.entities.Document;
import com.intranet.cic.entities.DocumentAccessLog;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.DocumentAccessLogRepository;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.DocumentService;
import com.intranet.cic.services.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;


@RestController
@RequestMapping(path = "/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController extends AbstractController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final DocumentAccessLogRepository accessLogRepository;

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

    @GetMapping("/{id}/logs")
    public ResponseEntity<Page<DocumentAccessLog>> getDocumentLogs(
            @PathVariable Long id,
            @PageableDefault(size = 50, sort = "accessedAt") Pageable pageable
    ) {
        documentService.getDocumentById(id); // ✅ 404 if not found
        return sendOkResponse(accessLogRepository.findByDocumentId(id, pageable));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> createDocument(
            @RequestPart("data") @Valid DocumentDTO documentDTO,
            @RequestPart("file") MultipartFile file
    ) {
        String fileUrl = fileStorageService.storeDocument(file);
        documentDTO.setFileUrl(fileUrl);
        return sendCreatedResponse(documentService.createDocument(documentDTO));
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
        fileStorageService.deleteFile(document.getFileUrl());
        documentService.deleteDocument(id);
        return sendNoContentResponse();
    }

    // ✅ Download — logs access
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            HttpServletRequest request,
            Authentication authentication
    ) {
        Document document = documentService.getDocumentById(id);

        if (!document.getAllowDownload()) {
            throw new IntranetException("Download not allowed for this document", HttpStatus.FORBIDDEN);
        }

        // ✅ Check if PRIVATE — only AUTHORIZED or ADMIN can access
        if (document.getAccess().name().equals("PRIVATE")) {
            boolean isMember = document.getMembers().stream()
                    .anyMatch(m -> m.getUser() != null &&
                            m.getUser().getUsername().equals(authentication.getName()));
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHORIZED"));
            if (!isMember && !isAdmin) {
                throw new IntranetException("Access denied", HttpStatus.FORBIDDEN);
            }
        }

        Resource resource = resolveResource(document.getFileUrl());
        logAccess(document, authentication, DocumentAccessLog.AccessAction.DOWNLOAD, request);

        String contentType = resolveContentType(document.getFileUrl());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + extractFileName(document.getFileUrl()) + "\"")
                .body(resource);
    }

    // ✅ View — logs access
    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long id,
            HttpServletRequest request,
            Authentication authentication
    ) {
        Document document = documentService.getDocumentById(id);

        if (!document.getAllowView()) {
            throw new IntranetException("View not allowed for this document", HttpStatus.FORBIDDEN);
        }

        // ✅ Check if PRIVATE
        if (document.getAccess().name().equals("PRIVATE")) {
            boolean isMember = document.getMembers().stream()
                    .anyMatch(m -> m.getUser() != null &&
                            m.getUser().getUsername().equals(authentication.getName()));
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHORIZED"));
            if (!isMember && !isAdmin) {
                throw new IntranetException("Access denied", HttpStatus.FORBIDDEN);
            }
        }

        Resource resource = resolveResource(document.getFileUrl());
        logAccess(document, authentication, DocumentAccessLog.AccessAction.VIEW, request);

        String contentType = resolveContentType(document.getFileUrl());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + extractFileName(document.getFileUrl()) + "\"")
                .body(resource);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────


    private Resource resolveResource(String fileUrl) {
        try {
            // ✅ Directly use the GCS public URL as a Resource
            Resource resource = new UrlResource(fileUrl);
            if (!resource.exists()) {
                throw new IntranetException("File not found", HttpStatus.NOT_FOUND);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IntranetException("File not found", HttpStatus.NOT_FOUND);
        }
    }

    private String resolveContentType(String fileUrl) {
        // ✅ Derive content type from file extension — no local file needed
        String lower = fileUrl.toLowerCase();
        if (lower.endsWith(".pdf"))  return "application/pdf";
        if (lower.endsWith(".doc"))  return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xls"))  return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".ppt"))  return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (lower.endsWith(".txt"))  return "text/plain";
        if (lower.endsWith(".csv"))  return "text/csv";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg"))  return "image/svg+xml";
        return "application/octet-stream"; // fallback
    }

    private void logAccess(Document document, Authentication authentication,
                           DocumentAccessLog.AccessAction action, HttpServletRequest request) {
        try {
            userRepository.findByUsername(authentication.getName()).ifPresent(user -> {
                DocumentAccessLog accessLog = new DocumentAccessLog();
                accessLog.setDocument(document);
                accessLog.setUser(user);
                accessLog.setAction(action);
                accessLog.setIpAddress(request.getRemoteAddr());
                accessLogRepository.save(accessLog);
                log.info("Access logged — user: {}, doc: {}, action: {}",
                        user.getUsername(), document.getId(), action);
            });
        } catch (Exception e) {
            // ✅ Never fail the request if logging fails
            log.warn("Failed to log document access for doc id: {}", document.getId(), e);
        }
    }

    private String extractFileName(String fileUrl) {
        // GCS URL: https://storage.googleapis.com/bucket/documents/uuid_filename.pdf
        return fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
    }
}