package com.intranet.cic.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class HeroShortcutGroupDTO {

    // ── Request ───────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotBlank(message = "Group name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        private String name;

        @NotNull(message = "Sort order is required")
        private Integer sortOrder;
    }

    // ── Response ──────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private Integer sortOrder;
        private List<HeroShortcutDTO.Response> shortcuts;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
