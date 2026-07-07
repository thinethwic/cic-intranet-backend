package com.intranet.cic.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskAttachmentDTO {
    private Long id;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadedAt;
}