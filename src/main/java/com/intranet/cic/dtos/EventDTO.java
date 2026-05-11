package com.intranet.cic.dtos;

import com.intranet.cic.entities.types.Segment;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class EventDTO {


    private Long id;                    // null on create, required on update

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    private String image;               // optional — URL/path as String

    @NotNull(message = "Date is required")
    @Future(message = "Event date must be in the future")
    private LocalDate date;

    @NotNull(message = "Time is required")
    private LocalTime time;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 200, message = "Location must be between 2 and 200 characters")
    private String location;

    private Segment segment;

    @NotNull(message = "User ID is required")
    private Long userId;
}
