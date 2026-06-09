package com.intranet.cic.dtos;

import com.intranet.cic.entities.TicketComment;
import com.intranet.cic.entities.User;
import com.intranet.cic.entities.types.Segment;
import com.intranet.cic.entities.types.TicketPriority;
import com.intranet.cic.entities.types.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TicketDTO {
    private Long id;

    private String ticketNumber;

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 2, max = 500, message = "Description must be between 2 and 500 characters")
    private String description;

    @NotNull(message = "Category by name is required")
    private String submittedByName;

    @NotNull(message = "Category is required")
    private String category;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    @NotNull(message = "Status is required")
    private TicketStatus status;

    private Segment segment;

    private User submittedBy;

    private User assignedTo;

    private String department;

    private String attachments;

    private List<TicketComment> comments = new ArrayList<>();

    private LocalDateTime createdAt;
}
