package com.intranet.cic.dtos;

import com.intranet.cic.entities.types.DocumentAccess;
import com.intranet.cic.entities.types.Category;
import com.intranet.cic.entities.types.DocumentType;
import com.intranet.cic.entities.types.Segment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class DocumentDTO {

    private Long id;                        // null on create, required on update

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    @NotNull(message = "Type is required")
    private DocumentType type;              // PDF, WORD, EXCEL, etc.

    @NotBlank(message = "File URL is required")
    private String fileUrl;                 // URL/path as String

    @NotNull(message = "Category is required")
    private Category category;        // HR, FINANCE, IT, etc.

    @NotNull(message = "Segment is required")
    private Segment segment;

    @NotNull(message = "Access level is required")
    private DocumentAccess access;          // PUBLIC, PRIVATE, RESTRICTED

    private Boolean isPinned = false;       // optional — defaults to false

    private Boolean allowView = true;       // optional — defaults to true

    private Boolean allowDownload = false;  // optional — defaults to false

    @NotNull(message = "Author is required")
    private Long createdById;               // ✅ just the ID, not the full User object

    private List<Long> memberIds;
}
