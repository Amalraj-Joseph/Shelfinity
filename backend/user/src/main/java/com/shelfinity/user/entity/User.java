/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.entity;

import java.time.Instant;
import java.time.LocalDate;

import com.shelfinity.user.dto.enums.Gender;
import com.shelfinity.user.dto.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * Entity representing a user in the system. Mapped to the "users" table in the
 * database.
 */
@Entity
@Table(name = "users")

@NamedQuery(name = "User.getAllUsers", query = "SELECT u FROM User u")
@NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email =:email")
@NamedQuery(name = "User.findByPhoneNumber", query = "SELECT u FROM User u WHERE u.phoneNumber =:phoneNumber")
@NamedQuery(name = "User.findByUsername", query = "SELECT u FROM User u WHERE u.username =:username")
@NamedQuery(name = "User.getAllUserEmails", query = "SELECT u.email FROM User u")
@NamedQuery(name = "User.getAllUserPhoneNumbers", query = "SELECT u.phoneNumber FROM User u")
@NamedQuery(name = "User.getAllUserUsernames", query = "SELECT u.phoneNumber FROM User u")

@NamedQuery(name = "User.updatePasswordById", query = "UPDATE User u SET u.password = :password, u.lastUpdated = :lastUpdated WHERE u.id = :id")
@NamedQuery(name = "User.updatePhoneNumberById", query = "UPDATE User u SET u.phoneNumber = :phoneNumber, u.lastUpdated = :lastUpdated WHERE u.id = :id")
@NamedQuery(name = "User.updateEmailById", query = "UPDATE User u SET u.email = :email, u.lastUpdated = :lastUpdated WHERE u.id = :id")
@NamedQuery(name = "User.updateUsernameById", query = "UPDATE User u SET u.username = :username, u.lastUpdated = :lastUpdated WHERE u.id = :id")
@NamedQuery(name = "User.updateRoleById", query = "UPDATE User u SET u.role = :role, u.lastUpdated = :lastUpdated WHERE u.id = :id")
@NamedQuery(name = "User.toggleLocked", query = "UPDATE User u SET u.locked = CASE WHEN u.locked = true THEN false ELSE true END, u.lastUpdated = :lastUpdated WHERE u.id = :id")
@NamedQuery(name = "User.toggleEnabled", query = "UPDATE User u SET u.enabled = CASE WHEN u.enabled = true THEN false ELSE true END, u.lastUpdated = :lastUpdated WHERE u.id = :id")
@NamedQuery(name = "User.updateLastLogin", query = "UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :id")
public class User extends BaseUserEntity {

    private Instant lastLogin;

    @Column(nullable = false)
    private boolean locked = false;

    @Column(nullable = false)
    private boolean enabled = true;

    public User() {
        // JPA default constructor
    }

    public User(Name name, LocalDate dateOfBirth, Gender gender, String username, String email, String password,
            String phoneNumber, Address address, Role role, Instant lastLogin, boolean locked,
            boolean enabled) {
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
        this.lastLogin = lastLogin;
        this.locked = locked;
        this.enabled = enabled;
    }

    // --- Getters and Setters ---
    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

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
        private Instant lastLogin;
        private boolean locked;
        private boolean enabled;

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

        public Builder lastLogin(Instant lastLogin) {
            this.lastLogin = lastLogin;
            return this;
        }

        public Builder locked(boolean locked) {
            this.locked = locked;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public User build() {
            return new User(
                    name, dateOfBirth, gender, username, email, password,
                    phoneNumber, address, role, lastLogin, locked, enabled
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
