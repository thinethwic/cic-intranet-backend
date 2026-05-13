package com.intranet.cic.dtos;

import com.intranet.cic.entities.types.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AlertDTO {

    private Long id;                        // null on create, required on update

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    @Size(max = 5000, message = "Body must not exceed 5000 characters")
    private String body;

    @NotNull(message = "Alert severity is required")
    private AlertSeverity alertSeverity;

    private LocalDate date;

    @Size(max = 2048, message = "Href must not exceed 2048 characters")
    private String href;

    private String flyerImage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @NotNull(message = "User ID is required")
    private Long userId;
}