package com.intranet.cic.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDTO {

    @Size(min = 2, max = 100)
    private String name;                // null = don't update

    @Email(message = "Invalid email address")
    private String email;               // null = don't update

    @Size(min = 8, max = 100)
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Must contain uppercase, lowercase and a number"
    )
    private String password;            // null = don't update

    private Boolean active;             // null = don't update
}
