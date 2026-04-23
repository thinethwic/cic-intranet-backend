package com.intranet.cic.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GalleryDTO {

    private Long id;                    // null on create, required on update

    @NotBlank(message = "Image URL is required")
    private String image;               // URL/path as String

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "User ID is required")
    private Long userId;
}
