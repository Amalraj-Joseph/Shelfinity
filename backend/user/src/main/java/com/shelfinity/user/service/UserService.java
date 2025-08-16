/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.shelfinity.common.logging.SFLoggable;
import com.shelfinity.common.logging.SFLogger;
import com.shelfinity.common.security.PasswordEncryptionService;
import com.shelfinity.user.dto.requests.RegisterUserRequestDTO;
import com.shelfinity.user.dto.requests.UpdateUserPasswordRequestDTO;
import com.shelfinity.user.dto.requests.UpdateUserProfileRequestDTO;
import com.shelfinity.user.entity.Address;
import com.shelfinity.user.entity.BaseUserEntity;
import com.shelfinity.user.entity.Name;
import com.shelfinity.user.entity.User;
import com.shelfinity.user.entity.UserRegistrationRequest;
import com.shelfinity.user.exception.DataBaseException;
import com.shelfinity.user.exception.UnauthorizedException;
import com.shelfinity.user.exception.UserAlreadyExistsException;
import com.shelfinity.user.exception.UserNotExistsException;
import com.shelfinity.user.exception.UserServiceException;
import com.shelfinity.user.repository.BaseUserRepository;
import com.shelfinity.user.repository.PreUserRepository;
import com.shelfinity.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@SFLoggable
@Transactional
public class UserService {

    private static final String CLASS_NAME = UserService.class.getName();
    private static String METHOD_NAME;

    @Inject
    private UserRepository userRepository;

    @Inject
    private PreUserRepository preUserRepository;

    @Inject
    private SFLogger logger;

    @Inject
    private PasswordEncryptionService passwordEncryptionService;

    public void registerUser(RegisterUserRequestDTO registerUserRequestDTO) {
        METHOD_NAME = "registerUser";
        validateRegistrationRequest(registerUserRequestDTO);
        String encryptedPassword = passwordEncryptionService.encryptPassword(registerUserRequestDTO.getPassword());

        Name name = new Name.Builder()
                            .salutation(registerUserRequestDTO.getName().getSalutation())
                            .firstName(registerUserRequestDTO.getName().getFirstName())
                            .middleName(registerUserRequestDTO.getName().getMiddleName())
                            .lastName(registerUserRequestDTO.getName().getLastName())
                            .build(); 
        Address address = new Address.Builder()
                            .street(registerUserRequestDTO.getAddress().getStreet())
                            .city(registerUserRequestDTO.getAddress().getCity())
                            .state(registerUserRequestDTO.getAddress().getState())
                            .postalCode(registerUserRequestDTO.getAddress().getPostalCode())
                            .country(registerUserRequestDTO.getAddress().getCountry())
                            .build() ;       
        UserRegistrationRequest newRequest = new UserRegistrationRequest.Builder()
                                                .name(name)
                                                .dateOfBirth(registerUserRequestDTO.getDateOfBirth())
                                                .gender(registerUserRequestDTO.getGender())
                                                .username(registerUserRequestDTO.getUsername())
                                                .email(registerUserRequestDTO.getEmail())
                                                .password(encryptedPassword)
                                                .phoneNumber(registerUserRequestDTO.getPhoneNumber())
                                                .address(address)
                                                .build();
        System.err.println("Request \n" + newRequest.getUpdatedBy().toString());
        insert(newRequest, preUserRepository);
    }

    public void addUser(UserRegistrationRequest request){
        METHOD_NAME = "addUser";
        User newUser = new User.Builder()
                        .name(request.getName())
                        .dateOfBirth(request.getDateOfBirth())
                        .gender(request.getGender())
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .password(request.getPassword())
                        .phoneNumber(request.getPhoneNumber())
                        .address(request.getAddress())
                        .role(request.getRole())
                        .build();
        insert(newUser, userRepository);
    }

    public void updateUserProfile(UpdateUserProfileRequestDTO updateUserProfileRequestDTO, UUID userId){
        User user = userRepository.getUserById(userId).get();
        if (user == null){
            throw new UserNotExistsException("Invalid User ID");
        }
        Name name       = user.getName();
        Address address = user.getAddress(); 

        Name updatedName        = new Name.Builder()
                                    .salutation(Objects.requireNonNullElse(updateUserProfileRequestDTO.getSalutation(), name.getSalutation()))
                                    .firstName(Objects.requireNonNullElse(updateUserProfileRequestDTO.getFirstName(), name.getFirstName()))
                                    .middleName(Objects.requireNonNullElse(updateUserProfileRequestDTO.getMiddleName(), name.getMiddleName()))
                                    .lastName(Objects.requireNonNullElse(updateUserProfileRequestDTO.getLastName(), name.getLastName()))
                                    .build();

        Address updatAddress    = new Address.Builder()
                                    .street(Objects.requireNonNullElse(updateUserProfileRequestDTO.getStreet(), address.getStreet()))
                                    .city(Objects.requireNonNullElse(updateUserProfileRequestDTO.getCity(), address.getCity()))
                                    .state(Objects.requireNonNullElse(updateUserProfileRequestDTO.getState(), address.getState()))
                                    .postalCode(Objects.requireNonNullElse(updateUserProfileRequestDTO.getPostalCode(), address.getPostalCode()))
                                    .country(Objects.requireNonNullElse(updateUserProfileRequestDTO.getCountry(), address.getCountry()))
                                    .build();
        
        if(!name.equals(updatedName)){
            user.setName(updatedName);
        }
        if(!address.equals(updatAddress)){
            user.setAddress(updatAddress);
        }
        if(updateUserProfileRequestDTO.getGender() != null){
            user.setGender(updateUserProfileRequestDTO.getGender());
        }
        if(updateUserProfileRequestDTO.getDateOfBirth() != null){
            user.setDateOfBirth(updateUserProfileRequestDTO.getDateOfBirth());
        }
        userRepository.updateUserProfile(user);
    }

    public List<UserRegistrationRequest> getRequests (){
        METHOD_NAME = "getRequests";
        return preUserRepository.getAllUsers();
    }

    public void updateUserPassword(UpdateUserPasswordRequestDTO updateUserPasswordRequestDTO, UUID userId){
        User user = userRepository.getUserById(userId).get();
        if (user == null){
            throw new UserNotExistsException("Invalid User ID");
        }
        if (!passwordEncryptionService.checkPassword(updateUserPasswordRequestDTO.getOldPassword(), user.getPassword())){
            throw new UnauthorizedException("The provided old password is wrong");
        }
        String encryptedNewPassword = passwordEncryptionService.encryptPassword(updateUserPasswordRequestDTO.getNewPassword());
        int actualRowsAffected = userRepository.updateUserPassword(userId, encryptedNewPassword);
        checkRowsAffected(1, actualRowsAffected);
    }

    public void updateUserPhoneNumber(String phoneNumber, UUID userId){
        checkUserExist(userId);
        checkPhoneNumberAvailability(phoneNumber);
        int actualRowsAffected = userRepository.updateUserPhoneNumber(userId, phoneNumber);
        checkRowsAffected(1, actualRowsAffected);
    }
    
    public void updateUserEmail(String email, UUID userId){
        checkUserExist(userId);
        checkEmailAvailability(email);
        int actualRowsAffected = userRepository.updateUserEmail(userId, email);
        checkRowsAffected(1, actualRowsAffected);
    }

    public void updateUserUsername(String username, UUID userId){
        checkUserExist(userId);
        validateUsername(username);
        int actualRowsAffected = userRepository.updateUserUsername(userId, username);
        checkRowsAffected(1, actualRowsAffected);
    }

    public void toggleLocked(UUID userId){
        checkUserExist(userId);
        int actualRowsAffected = userRepository.toggleUserLock(userId);
        checkRowsAffected(1, actualRowsAffected);
    }

    public void toggleEnabled(UUID userId){
        checkUserExist(userId);
        int actualRowsAffected = userRepository.toggleUserEnabled(userId);
        checkRowsAffected(1, actualRowsAffected);
    }

    public void updateLastLogin(UUID userId){
        checkUserExist(userId);
        int actualRowsAffected = userRepository.updateLastLogin(userId);
        checkRowsAffected(1, actualRowsAffected);
    }

    public UUID checkAuthorization(String username, String password ){
        User user = userRepository.getUserByUsername(username).get();
        if(user == null){
            throw new UserNotExistsException("Invalid Username");
        }
        if(!passwordEncryptionService.checkPassword(password, user.getPassword())){
            throw new UnauthorizedException("The provided old password is wrong");
        }
        return user.getId();
    }

    public void checkUserExist(UUID userId){
        User user = userRepository.getUserById(userId).get();
        if (user == null){
            throw new UserNotExistsException("Invalid User ID");
        }
    }

    private <T extends BaseUserEntity, P extends BaseUserRepository<T>> void insert(T user, P repository){
        METHOD_NAME = "insert";
        try{
            // Retry mechanism (basic retry for database operation)
            boolean success = false;
            int retryCount = 3;
            while (retryCount-- > 0 && !success) {
                try {
                    repository.addUser(user);
                    success = true;
                } catch (Exception e) {
                    logger.warning(CLASS_NAME, METHOD_NAME, "Failed to add user, retrying...");
                    if (retryCount == 0) {
                        throw new UserServiceException("Failed to add user after multiple attempts", e);
                    }
                }
            }

        } catch (UserAlreadyExistsException e) {
            throw e; // rethrow the exception to the controller
        } catch (Exception e) {
            throw new UserServiceException("An error occurred while registering the user", e);
        }
    }

    private void validateRegistrationRequest(RegisterUserRequestDTO registerUserRequestDTO){
        validatePhoneNumber(registerUserRequestDTO.getPhoneNumber());
        validateEmail(registerUserRequestDTO.getEmail());
        validateUsername(registerUserRequestDTO.getUsername());
    }

    private void checkPhoneNumberAvailability(String phoneNumber){
        validatePhoneNumber(phoneNumber);
        //TODO: Implement 2FA
    }

    private void checkEmailAvailability(String email){
        validateEmail(email);
        //TODO: Implement 2FA
    }

    private void validatePhoneNumber(String phoneNumber){
        Optional<User> existingUser = userRepository.getUserByPhoneNumber(phoneNumber);
        Optional<UserRegistrationRequest> request = preUserRepository.getUserByPhoneNumber(phoneNumber);
        if(existingUser.isPresent() || request.isPresent()){
            throw new UserAlreadyExistsException("User or registration request with this Phone Number already exists.");
        }
    }

    private void validateEmail(String email){
        Optional<User> existingUser = userRepository.getUserByEmail(email);
        Optional<UserRegistrationRequest> request = preUserRepository.getUserByEmail(email);
        if(existingUser.isPresent() || request.isPresent()){
            throw new UserAlreadyExistsException("User or registration request with this email already exists.");
        }       
    }

    private void validateUsername(String username){
        Optional<User> existingUser = userRepository.getUserByUsername(username);
        Optional<UserRegistrationRequest> request = preUserRepository.getUserByUsername(username);
        if(existingUser.isPresent() || request.isPresent()){
            throw new UserAlreadyExistsException("User or registration request with this username already exists.");
        }
    }

    private void checkRowsAffected(int expected, int actual){
        if(expected != actual){
            throw new DataBaseException("A database exception occured");
        }
    }
}

