package com.intranet.cic.dtos;

import com.intranet.cic.entities.types.Segment;
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
    private Segment segment;    // ← add
    private String department;
}
