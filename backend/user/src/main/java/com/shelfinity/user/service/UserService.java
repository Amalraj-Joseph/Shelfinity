/*
 * MIT License
 * 
 * Copyright (c) 2025 Shadow-Codex
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.shelfinity.user.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

import com.shelfinity.user.dto.requests.RegisterUserRequestDTO;
import com.shelfinity.user.entity.User;
import com.shelfinity.user.exception.UserAlreadyExistsException;
import com.shelfinity.user.exception.UserServiceException;
import com.shelfinity.user.repository.UserRepository;
import com.shelfinity.common.logging.SFLoggable;
import com.shelfinity.common.logging.SFLogger;
import com.shelfinity.common.security.PasswordEncryptionService;
import com.shelfinity.user.dto.enums.Role;

@ApplicationScoped
@SFLoggable
public class UserService {

    @Inject
    private UserRepository userRepository;

    private final SFLogger logger;
    private final PasswordEncryptionService passwordEncryptionService;

    @Inject
    public UserService(PasswordEncryptionService passwordEncryptionService, SFLogger logger) {
        this.passwordEncryptionService = passwordEncryptionService;
        this.logger = logger;
    }

    @Transactional
    public void registerUser(RegisterUserRequestDTO registrationRequestDTO) {
        try {
            // Check if the email already exists
            Optional<User> existingUser = userRepository.getUserByEmail(registrationRequestDTO.getEmail());
            if (existingUser.isPresent()) {
                throw new UserAlreadyExistsException("User with this email already exists.");
            }

            // Encrypt the password
            String encryptedPassword = passwordEncryptionService.encryptPassword(registrationRequestDTO.getPassword());

            // Create a new User entity
            User newUser = new User(
                registrationRequestDTO.getName(),
                registrationRequestDTO.getEmail(),
                encryptedPassword,
                registrationRequestDTO.getPhoneNumber(),
                registrationRequestDTO.getAddress(),
                Role.USER, // Default role
                LocalDateTime.now(), // createdAt
                null, // lastLogin
                false, // locked
                false  // enabled
            );

            // Retry mechanism (basic retry for database operation)
            boolean success = false;
            int retryCount = 3;
            while (retryCount-- > 0 && !success) {
                try {
                    userRepository.addUser(newUser);
                    success = true;
                } catch (Exception e) {
                    logger.warning("Failed to add user, retrying...");
                    if (retryCount == 0) {
                        throw new UserServiceException("Failed to register user after multiple attempts", e);
                    }
                }
            }

        } catch (UserAlreadyExistsException e) {
            throw e; // rethrow the exception to the controller
        } catch (Exception e) {
            throw new UserServiceException("An error occurred while registering the user", e);
        }
    }
}

