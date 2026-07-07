package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.TaskAttachmentDTO;
import com.intranet.cic.dtos.TaskDTO;
import com.intranet.cic.dtos.TaskDocumentDTO;
import com.intranet.cic.entities.TaskAttachment;
import com.intranet.cic.entities.types.TaskPriority;
import com.intranet.cic.entities.types.TaskStatus;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.services.FileStorageService;
import com.intranet.cic.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TaskController extends AbstractController {

    private final TaskService taskService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        return sendCreatedResponse(taskService.createTask(taskDTO));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<TaskDTO>> getMyTasks(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String q) {
        return sendOkResponse(taskService.getMyTasks(pageable, status, priority, q));
    }

    // Aggregated "my documents" view across every task — registered ahead of
    // /{id} textually for readability; Spring routes by segment count so
    // there's no actual collision with the single-segment /{id} mapping.
    @GetMapping("/attachments/my")
    public ResponseEntity<Page<TaskDocumentDTO>> getMyDocuments(
            @PageableDefault(size = 20, sort = "uploadedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String q) {
        return sendOkResponse(taskService.getMyDocuments(pageable, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return sendOkResponse(taskService.getTaskById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        return sendOkResponse(taskService.updateTask(id, taskDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return sendNoContentResponse();
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TaskAttachmentDTO> uploadAttachment(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        return sendCreatedResponse(taskService.uploadAttachment(id, file));
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<TaskAttachmentDTO>> getAttachments(@PathVariable Long id) {
        return sendOkResponse(taskService.getAttachments(id));
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id, @PathVariable Long attachmentId) {
        taskService.deleteAttachment(id, attachmentId);
        return sendNoContentResponse();
    }

    @GetMapping("/{id}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long id, @PathVariable Long attachmentId) {
        TaskAttachment attachment = taskService.getOwnedAttachment(id, attachmentId);
        Resource resource = resolveResource(attachment.getFileUrl());
        String contentType = attachment.getContentType() != null
                ? attachment.getContentType()
                : "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    // Reads directly off local disk via FileStorageService, same as
    // DocumentController — not a bare UrlResource(fileUrl), since fileUrl
    // points at the nginx-served /uploads/ endpoint, not a fetchable blob.
    private Resource resolveResource(String fileUrl) {
        try {
            Path filePath = fileStorageService.resolveFilePath(fileUrl);
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new IntranetException("File not found", HttpStatus.NOT_FOUND);
            }
            return resource;
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            throw new IntranetException("File not found", HttpStatus.NOT_FOUND);
        }
    }
}