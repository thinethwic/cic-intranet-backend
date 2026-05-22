package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.UpdateUserDTO;
import com.intranet.cic.dtos.UserDTO;
import com.intranet.cic.entities.User;
import com.intranet.cic.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController extends AbstractController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return sendOkResponse(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return sendOkResponse(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<User> createUser(
            @Valid @RequestBody UserDTO userDTO
    ) {
        User user = userService.createUser(userDTO);
        return sendCreatedResponse(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDTO userDTO
    ) {
        User user = userService.updateUser(id, userDTO);
        return sendOkResponse(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return sendNoContentResponse();
    }
}