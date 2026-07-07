package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.TaskAttachmentDTO;
import com.intranet.cic.dtos.TaskDTO;
import com.intranet.cic.dtos.TaskDocumentDTO;
import com.intranet.cic.entities.Task;
import com.intranet.cic.entities.TaskAttachment;
import com.intranet.cic.entities.User;
import com.intranet.cic.entities.types.TaskPriority;
import com.intranet.cic.entities.types.TaskStatus;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.TaskAttachmentRepository;
import com.intranet.cic.repositories.TaskRepository;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.FileStorageService;
import com.intranet.cic.services.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {

    private static final List<String> IMAGE_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp", "svg");

    private final TaskRepository taskRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    private User getCurrentUser() {
        try {
            String username = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new IntranetException(
                            "Authenticated user not found: " + username, HttpStatus.UNAUTHORIZED));
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to resolve authenticated user", e);
            throw new IntranetException("Failed to resolve authenticated user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Tasks are personal — a task belonging to someone else is reported as
    // not found rather than forbidden, so its existence isn't leaked.
    private Task getOwnedTask(Long id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IntranetException(
                        "Task not found with id: " + id, HttpStatus.NOT_FOUND));
        if (!task.getOwner().getId().equals(currentUser.getId())) {
            throw new IntranetException("Task not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        return task;
    }

    private TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        User owner = task.getOwner();
        TaskDTO.OwnerDTO ownerDTO = new TaskDTO.OwnerDTO();
        ownerDTO.setId(owner.getId());
        ownerDTO.setName(owner.getName());
        ownerDTO.setEmail(owner.getEmail());
        dto.setOwner(ownerDTO);

        dto.setAttachments(task.getAttachments().stream().map(this::toAttachmentDTO).toList());
        return dto;
    }

    private TaskAttachmentDTO toAttachmentDTO(TaskAttachment attachment) {
        TaskAttachmentDTO dto = new TaskAttachmentDTO();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileUrl(attachment.getFileUrl());
        dto.setFileSize(attachment.getFileSize());
        dto.setContentType(attachment.getContentType());
        dto.setUploadedAt(attachment.getUploadedAt());
        return dto;
    }

    private TaskDocumentDTO toDocumentDTO(TaskAttachment attachment) {
        TaskDocumentDTO dto = new TaskDocumentDTO();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileUrl(attachment.getFileUrl());
        dto.setFileSize(attachment.getFileSize());
        dto.setContentType(attachment.getContentType());
        dto.setUploadedAt(attachment.getUploadedAt());
        dto.setTaskId(attachment.getTask().getId());
        dto.setTaskTitle(attachment.getTask().getTitle());
        return dto;
    }

    // Task attachments may be office documents or images — dispatch to
    // whichever of FileStorageService's two allow-listed methods fits.
    private String storeAttachmentFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (IMAGE_EXTENSIONS.contains(extension)) {
            return fileStorageService.storeImage(file);
        }
        return fileStorageService.storeDocument(file);
    }

    @Override
    public TaskDTO createTask(TaskDTO taskDTO) {
        try {
            User currentUser = getCurrentUser();

            Task task = new Task();
            task.setTitle(taskDTO.getTitle().trim());
            task.setDescription(taskDTO.getDescription());
            task.setPriority(taskDTO.getPriority());
            task.setDueDate(taskDTO.getDueDate());
            task.setStatus(TaskStatus.TODO);
            task.setOwner(currentUser);

            return toDTO(taskRepository.save(task));
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create task", e);
            throw new IntranetException("Failed to create task", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDTO> getMyTasks(Pageable pageable, TaskStatus status, TaskPriority priority, String q) {
        try {
            User currentUser = getCurrentUser();
            String query = (q == null || q.isBlank()) ? null : q.trim();
            return taskRepository.findMyTasks(currentUser, status, priority, query, pageable)
                    .map(this::toDTO);
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch tasks for current user", e);
            throw new IntranetException("Failed to fetch your tasks", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long id) {
        return toDTO(getOwnedTask(id));
    }

    @Override
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        try {
            Task task = getOwnedTask(id);

            if (taskDTO.getTitle() != null) task.setTitle(taskDTO.getTitle().trim());
            if (taskDTO.getDescription() != null) task.setDescription(taskDTO.getDescription());
            if (taskDTO.getPriority() != null) task.setPriority(taskDTO.getPriority());
            if (taskDTO.getStatus() != null) task.setStatus(taskDTO.getStatus());
            task.setDueDate(taskDTO.getDueDate());

            return toDTO(taskRepository.save(task));
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update task id: {}", id, e);
            throw new IntranetException("Failed to update task", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteTask(Long id) {
        try {
            Task task = getOwnedTask(id);
            List<TaskAttachment> attachments = task.getAttachments();
            taskRepository.delete(task);
            attachments.forEach(a -> fileStorageService.deleteFile(a.getFileUrl()));
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete task id: {}", id, e);
            throw new IntranetException("Failed to delete task", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public TaskAttachmentDTO uploadAttachment(Long taskId, MultipartFile file) {
        try {
            Task task = getOwnedTask(taskId);

            String fileUrl = storeAttachmentFile(file);

            TaskAttachment attachment = new TaskAttachment();
            attachment.setTask(task);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileUrl(fileUrl);
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());

            return toAttachmentDTO(taskAttachmentRepository.save(attachment));
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload attachment for task id: {}", taskId, e);
            throw new IntranetException("Failed to upload attachment", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAttachmentDTO> getAttachments(Long taskId) {
        Task task = getOwnedTask(taskId);
        return taskAttachmentRepository.findByTaskOrderByUploadedAtAsc(task).stream()
                .map(this::toAttachmentDTO)
                .toList();
    }

    @Override
    public void deleteAttachment(Long taskId, Long attachmentId) {
        try {
            TaskAttachment attachment = getOwnedAttachment(taskId, attachmentId);
            taskAttachmentRepository.delete(attachment);
            fileStorageService.deleteFile(attachment.getFileUrl());
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete attachment id: {} for task id: {}", attachmentId, taskId, e);
            throw new IntranetException("Failed to delete attachment", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TaskAttachment getOwnedAttachment(Long taskId, Long attachmentId) {
        Task task = getOwnedTask(taskId);
        return taskAttachmentRepository.findByIdAndTask(attachmentId, task)
                .orElseThrow(() -> new IntranetException(
                        "Attachment not found with id: " + attachmentId, HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDocumentDTO> getMyDocuments(Pageable pageable, String q) {
        try {
            User currentUser = getCurrentUser();
            String query = (q == null || q.isBlank()) ? null : q.trim();
            return taskAttachmentRepository.findMyDocuments(currentUser, query, pageable)
                    .map(this::toDocumentDTO);
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch documents for current user", e);
            throw new IntranetException("Failed to fetch your documents", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}