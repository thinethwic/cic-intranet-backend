package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.UpdateUserDTO;
import com.intranet.cic.dtos.UserDTO;
import com.intranet.cic.entities.User;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        try {
            return userRepository.findAll(pageable);
        } catch (Exception exception) {
            log.error("Failed to get all users", exception);
            throw new IntranetException("Failed to get all users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public User getUserById(Long id) {
        try {
            return userRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("User not found", HttpStatus.NOT_FOUND));
        } catch (IntranetException intranetException) {
            log.warn("User not found with id: {} to fetch", id, intranetException);
            throw intranetException;                          // ✅ re-throw original
        } catch (Exception exception) {
            log.error("Error getting user", exception);
            throw new IntranetException("Failed to get user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public User createUser(UserDTO userDTO) {
        try {
            if (userRepository.existsByUsername(userDTO.getUsername())) {
                throw new IntranetException("Username already exists", HttpStatus.CONFLICT);
            }
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new IntranetException("Email already exists", HttpStatus.CONFLICT);
            }

            User user = modelMapper.map(userDTO, User.class);
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

            return userRepository.save(user);
        } catch (IntranetException intranetException) {
            log.warn("Conflict while creating user: {}", intranetException.getMessage());
            throw intranetException;
        } catch (Exception exception) {
            log.error("Failed to create user", exception);
            throw new IntranetException("Failed to create user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public User updateUser(Long id, UpdateUserDTO userDTO) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("User not found", HttpStatus.NOT_FOUND));

            modelMapper.map(userDTO, user);

            if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }

            return userRepository.save(user);
        } catch (IntranetException intranetException) {
            log.warn("User not found with id: {} to update", id, intranetException);
            throw intranetException;                          // ✅ re-throw original
        } catch (Exception exception) {
            log.error("Error updating user", exception);
            throw new IntranetException("Failed to update user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteUser(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("User not found", HttpStatus.NOT_FOUND));

            userRepository.delete(user);

        } catch (IntranetException intranetException) {
            log.warn("User not found with id: {} to delete", id, intranetException);
            throw intranetException;

        } catch (DataIntegrityViolationException e) {
            log.warn("Cannot delete user id: {} — still referenced by other records", id, e);
            throw new IntranetException(
                    "Cannot delete this user because they have associated records (documents, logs, or tickets). " +
                            "Please remove those first before deleting the user.",
                    HttpStatus.CONFLICT
            );

        } catch (Exception exception) {
            log.error("Error deleting user", exception);
            throw new IntranetException("Failed to delete user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
