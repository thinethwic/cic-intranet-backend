package com.intranet.cic.dtos;

import com.intranet.cic.entities.types.Segment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

// ── Request ───────────────────────────────────────────────────────────────────

public class DepartmentDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotBlank(message = "Department name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        private String name;

        @NotBlank(message = "Department code is required")
        @Size(max = 30, message = "Code must not exceed 30 characters")
        @Pattern(regexp = "^[A-Z0-9_\\-]+$",
                message = "Code must contain only uppercase letters, digits, underscores or hyphens")
        private String code;

        @NotNull(message = "Segment is required")
        private Segment segment;
    }

    // ── Response ──────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String code;
        private Segment segment;
        private Date createdAt;
        private Date updatedAt;
    }
}

