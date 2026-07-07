package com.intranet.cic.services;

import com.intranet.cic.dtos.TaskAttachmentDTO;
import com.intranet.cic.dtos.TaskDTO;
import com.intranet.cic.dtos.TaskDocumentDTO;
import com.intranet.cic.entities.TaskAttachment;
import com.intranet.cic.entities.types.TaskPriority;
import com.intranet.cic.entities.types.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskService {

    TaskDTO createTask(TaskDTO taskDTO);

    Page<TaskDTO> getMyTasks(Pageable pageable, TaskStatus status, TaskPriority priority, String q);

    TaskDTO getTaskById(Long id);

    TaskDTO updateTask(Long id, TaskDTO taskDTO);

    void deleteTask(Long id);

    TaskAttachmentDTO uploadAttachment(Long taskId, MultipartFile file);

    List<TaskAttachmentDTO> getAttachments(Long taskId);

    void deleteAttachment(Long taskId, Long attachmentId);

    TaskAttachment getOwnedAttachment(Long taskId, Long attachmentId);

    Page<TaskDocumentDTO> getMyDocuments(Pageable pageable, String q);
}