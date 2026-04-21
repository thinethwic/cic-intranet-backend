package com.intranet.cic.services;

import com.intranet.cic.dtos.UpdateUserDTO;
import com.intranet.cic.dtos.UserDTO;
import com.intranet.cic.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<User> getAllUsers(Pageable pageable);
    User getUserById(Long id);
    User createUser(UserDTO userDTO);
    User updateUser(Long id, UpdateUserDTO userDTO);
    void deleteUser(Long id);
}
