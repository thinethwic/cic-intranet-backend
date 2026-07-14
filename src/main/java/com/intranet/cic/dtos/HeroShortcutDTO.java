package com.intranet.cic.dtos;

import com.intranet.cic.entities.types.HeroShortcutColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class HeroShortcutDTO {

    // ── Request ───────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotBlank(message = "Label is required")
        @Size(max = 100, message = "Label must not exceed 100 characters")
        private String label;

        @NotBlank(message = "URL is required")
        @Size(max = 2048, message = "URL must not exceed 2048 characters")
        private String url;

        @NotBlank(message = "Icon name is required")
        @Size(max = 100, message = "Icon name must not exceed 100 characters")
        private String iconName;

        @NotNull(message = "Color is required")
        private HeroShortcutColor color;

        @NotNull(message = "Group ID is required")
        private Long groupId;

        @NotNull(message = "Sort order is required")
        private Integer sortOrder;
    }

    // ── Response ──────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String label;
        private String url;
        private String iconName;
        private HeroShortcutColor color;
        private Long groupId;
        private Integer sortOrder;
    }
}
