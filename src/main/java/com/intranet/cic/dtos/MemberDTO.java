package com.intranet.cic.dtos;

import com.intranet.cic.entities.types.MemberRole;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberDTO {

    private Long id;                    // null on create, required on update

    @NotBlank(message = "Title is required")
    @Size(max = 20, message = "Title must not exceed 20 characters")
    private String title;               // Mr, Mrs, Dr, Prof, etc.

    @NotNull(message = "Role is required")
    private MemberRole role;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(
            regexp = "^\\+?[0-9]{7,15}$",
            message = "Phone number must be between 7 and 15 digits, optionally starting with +"
    )
    private String phoneNo;

    private LocalDate joinedDate;

    private Long user;
}
