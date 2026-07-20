package com.intranet.cic.dtos;

import com.intranet.cic.entities.types.Category;
import com.intranet.cic.entities.types.Segment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementDTO {

    private Long id;                        // null on create, required on update

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Description is too long")
    private String description;

    private String image;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Segment is required")
    private Segment segment;

    private Boolean isRead = false;

    @NotNull(message = "User ID is required")
    private Long userId;
}
