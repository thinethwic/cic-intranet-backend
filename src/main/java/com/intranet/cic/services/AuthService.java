package com.intranet.cic.services;

import com.intranet.cic.dtos.LoginDTO;
import com.intranet.cic.dtos.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginDTO loginDTO);
}
