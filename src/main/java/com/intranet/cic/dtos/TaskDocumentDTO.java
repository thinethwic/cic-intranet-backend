package com.intranet.cic.dtos;

import lombok.Data;

import java.time.LocalDateTime;

// An attachment as it appears in the aggregated "my documents" view —
// same file, plus which task it belongs to.
@Data
public class TaskDocumentDTO {
    private Long id;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadedAt;
    private Long taskId;
    private String taskTitle;
}