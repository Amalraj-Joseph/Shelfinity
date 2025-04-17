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
package com.shelfinity.user.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.shelfinity.user.dto.enums.Role;

/**
 * Entity representing a user in the system.
 * Mapped to the "users" table in the database.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Primary key email address of the user.
     */
    @Id
    @Column(length = 100, nullable = false, unique = true)
    private String email;

    /**
     * Full name of the user.
     */
    @Column(length = 100, nullable = false)
    private String name;

    /**
     * Encrypted password of the user.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Contact phone number of the user.
     */
    @Column(length = 15, nullable = false)
    private String phoneNumber;

    /**
     * Residential or mailing address of the user.
     */
    @Column(length = 255, nullable = false)
    private String address;

    /**
     * Role assigned to the user (e.g., ADMIN, USER).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Timestamp of when the user was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the user's most recent login.
     */
    private LocalDateTime lastLogin;

    /**
     * Indicates whether the user account is locked.
     */
    @Column(nullable = false)
    private boolean locked;

    /**
     * Indicates whether the user account is enabled and can be used for login.
     */
    @Column(nullable = false)
    private boolean enabled;

    /**
     * Default constructor for JPA.
     */
    public User() {
        // JPA default constructor
    }

    /**
     * Constructs a User instance with all required fields.
     *
     * @param name        the user's full name
     * @param email       the user's email address (primary key)
     * @param password    the user's password (encrypted)
     * @param phoneNumber the user's phone number
     * @param address     the user's address
     * @param role        the user's role
     * @param createdAt   creation timestamp
     * @param lastLogin   last login timestamp
     * @param locked      account lock status
     * @param enabled     account enabled status
     */
    public User(String name, String email, String password, String phoneNumber, String address, Role role,
                LocalDateTime createdAt, LocalDateTime lastLogin, boolean locked, boolean enabled) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.locked = locked;
        this.enabled = enabled;
    }

    // --- Getters and setters with standard naming ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
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

    /**
     * Compares this user to another based on their email address.
     *
     * @param o other object
     * @return true if emails are the same, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(email, user.email);
    }

    /**
     * Computes hash code using the email field.
     *
     * @return hash code of the user
     */
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
