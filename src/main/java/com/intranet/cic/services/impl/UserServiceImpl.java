package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.UpdateUserDTO;
import com.intranet.cic.dtos.UserDTO;
import com.intranet.cic.entities.Document;
import com.intranet.cic.entities.User;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
        try{
            return userRepository.findAll(pageable);
        } catch (Exception exception){
            log.error("Failed to get all users", exception);
            throw new IntranetException("Failed to get all users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public User getUserById(Long id) {
        try{
            return userRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("User Not found", HttpStatus.NOT_FOUND)
                    );
        } catch (IntranetException intranetException) {

            log.warn("User not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("User Not found", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            log.error("Error getting user", exception);
            throw new IntranetException("Failed to get user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public User createUser(UserDTO userDTO) {
        try {
            User user = modelMapper.map(userDTO, User.class);
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            return userRepository.save(user);
        } catch (Exception exception) {
            log.error("Failed to create User", exception);
            throw new IntranetException("Failed to create User", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public User updateUser(Long id, UpdateUserDTO userDTO) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("User Not found", HttpStatus.NOT_FOUND));

            modelMapper.map(userDTO, user);

            if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }

            return userRepository.save(user);
        } catch (IntranetException intranetException) {
            log.warn("User not found with id: {} to update", id, intranetException);
            throw intranetException;
        } catch (Exception exception) {
            log.error("Error updating User", exception);
            throw new IntranetException("Failed to update User", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteUser(Long id) {
        try{
            User user = userRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("User Not found", HttpStatus.NOT_FOUND)
                    );


            userRepository.delete(user);
        }  catch (IntranetException intranetException) {

            log.warn("User not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("User Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {

            log.error("Error updating User", exception);
            throw new IntranetException("Failed to update User", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
