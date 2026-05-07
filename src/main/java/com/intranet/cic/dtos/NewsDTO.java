package com.intranet.cic.dtos;

import com.intranet.cic.entities.types.Segment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 2, max = 500, message = "Description must be between 2 and 500 characters")
    private String description;

    @Size(max = 50000, message = "Content is too long")
    private String content;                 // optional

    private String image;                   // optional — URL/path as String

    @NotNull(message = "Category is required")
    private Segment category;          // enum, not a String

    @NotNull(message = "Author is required")
    private Long authorId;                  // ✅ just the ID, not the full User object

    @NotNull(message = "isHot is required")
    private Boolean isHot = false;

    private LocalDateTime hotSince;
}
