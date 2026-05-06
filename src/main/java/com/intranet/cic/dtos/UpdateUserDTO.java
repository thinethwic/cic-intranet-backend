package com.intranet.cic.dtos;

import com.intranet.cic.entities.types.Segment;
import com.intranet.cic.entities.types.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDTO {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "Username can only contain letters, numbers and underscores"
    )
    private String username;

    // Optional — only update if provided
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;

    @Size(min = 2, max = 100)
    private String name;

    @Email
    @Size(max = 100)
    private String email;

    private UserRole role;

    private Boolean active;

    private Segment segment;

    private String department;
}
