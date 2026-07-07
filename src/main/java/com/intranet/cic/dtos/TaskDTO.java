package com.intranet.cic.dtos;

import com.intranet.cic.entities.types.TaskPriority;
import com.intranet.cic.entities.types.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TaskDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    private String description;

    private TaskStatus status;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    private LocalDate dueDate;

    private OwnerDTO owner;

    private List<TaskAttachmentDTO> attachments = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Data
    public static class OwnerDTO {
        private Long id;
        private String name;
        private String email;
    }
}