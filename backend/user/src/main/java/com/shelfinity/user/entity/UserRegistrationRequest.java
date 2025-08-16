/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.entity;

import java.time.LocalDate;
import java.util.UUID;

import com.shelfinity.user.dto.enums.Gender;
import com.shelfinity.user.dto.enums.RegistrationStatus;
import com.shelfinity.user.dto.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_reg_requests")

@NamedQuery(name = "UserRegistrationRequest.getAllUsers", query = "SELECT u FROM UserRegistrationRequest u")
@NamedQuery(name = "UserRegistrationRequest.findByEmail", query = "SELECT u FROM UserRegistrationRequest u WHERE u.email =:email")
@NamedQuery(name = "UserRegistrationRequest.findByPhoneNumber", query = "SELECT u FROM UserRegistrationRequest u WHERE u.phoneNumber =:phoneNumber")
@NamedQuery(name = "UserRegistrationRequest.findByUsername", query = "SELECT u FROM UserRegistrationRequest u WHERE u.username =:username")
@NamedQuery(name = "UserRegistrationRequest.getAllUserEmails", query = "SELECT u.email FROM UserRegistrationRequest u")
@NamedQuery(name = "UserRegistrationRequest.getAllUserPhoneNumbers", query = "SELECT u.phoneNumber FROM UserRegistrationRequest u")
@NamedQuery(name = "UserRegistrationRequest.getAllUserUsernames", query = "SELECT u.phoneNumber FROM UserRegistrationRequest u")
public class UserRegistrationRequest extends BaseUserEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    private UUID updatedBy;

    @Column(length = 255)
    private String remark;

    // No-args constructor (JPA requirement)
    public UserRegistrationRequest() { }

    // All-args constructor (excluding generated id)
    public UserRegistrationRequest(Name name, LocalDate dateOfBirth, Gender gender, String username, 
                                   String email, String password, String phoneNumber, Address address, 
                                   Role role, RegistrationStatus status, UUID updatedBy, String remark) {
        super(
            name,
            dateOfBirth, 
            gender, 
            username,
            email,
            password,
            phoneNumber,
            address,
            role
        );
        this.status = status != null ? status : RegistrationStatus.PENDING;
        this.updatedBy = updatedBy != null ? updatedBy : new UUID(0L,0L);
    }

    // Getters and setters

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
    // Static Builder Class
    public static class Builder {

        private Name name;
        private LocalDate dateOfBirth;
        private Gender gender;
        private String username;
        private String email;
        private String password;
        private String phoneNumber;
        private Address address;
        private Role role;
        private RegistrationStatus status = RegistrationStatus.PENDING;
        private UUID updatedBy;
        private String remark;

        public Builder name(Name name) {
            this.name = name;
            return this;
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder address(Address address) {
            this.address = address;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder status(RegistrationStatus status) {
            this.status = status;
            return this;
        }

        
        public Builder updatedBy(UUID updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public UserRegistrationRequest build() {
            return new UserRegistrationRequest(
                    name, dateOfBirth, gender, username, email, password, 
                    phoneNumber, address, role, status, updatedBy, remark);
        }
    }
}
