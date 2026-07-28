package com.intranet.cic.dtos;

import com.intranet.cic.entities.types.Segment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NavItemDTO {

    private Long id;                    // null on create, required on update

    @NotBlank(message = "Label is required")
    @Size(max = 100, message = "Label must not exceed 100 characters")
    private String label;

    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String url;                 // optional — null means dropdown-only folder

    private Segment segment;            // null means it applies to all companies

    private Long parentId;              // null means top-level nav item
}
