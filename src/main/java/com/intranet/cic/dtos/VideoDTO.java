package com.intranet.cic.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VideoDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 2, max = 1000, message = "Description must be between 2 and 1000 characters")
    private String description;

    @NotBlank(message = "Video link is required")
    @Pattern(
            regexp = "^(https?://).*",
            message = "Video link must be a valid URL starting with http:// or https://"
    )
    private String videoLink;

    private Long userId;
}
