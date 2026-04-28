package com.intranet.cic.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private String username;
    private String role; // ✅ add this
}
