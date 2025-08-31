/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.email;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Email configuration entity for SMTP server settings.
 */
@Entity
@Table(name = "email_config")
@NamedQueries({
    @NamedQuery(name = "EmailConfig.findActive", query = "SELECT e FROM EmailConfig e WHERE e.active = true ORDER BY e.createdAt DESC"),
    @NamedQuery(name = "EmailConfig.findAll", query = "SELECT e FROM EmailConfig e ORDER BY e.createdAt DESC")
})
public class EmailConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Column(name = "smtp_host", nullable = false)
    private String smtpHost;
    
    @Positive
    @Column(name = "smtp_port", nullable = false)
    private int smtpPort = 587;
    
    @NotBlank
    @Email
    @Column(name = "sender_email", nullable = false)
    private String senderEmail;
    
    @Column(name = "sender_name")
    private String senderName;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "use_tls", nullable = false)
    private boolean useTls = true;
    
    @Column(name = "use_ssl", nullable = false)
    private boolean useSsl = false;
    
    @Column(name = "require_auth", nullable = false)
    private boolean requireAuth = true;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public EmailConfig() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with required fields
    public EmailConfig(String smtpHost, int smtpPort, String senderEmail) {
        this();
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.senderEmail = senderEmail;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getSmtpHost() {
        return smtpHost;
    }
    
    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }
    
    public int getSmtpPort() {
        return smtpPort;
    }
    
    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }
    
    public String getSenderEmail() {
        return senderEmail;
    }
    
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isUseTls() {
        return useTls;
    }
    
    public void setUseTls(boolean useTls) {
        this.useTls = useTls;
    }
    
    public boolean isUseSsl() {
        return useSsl;
    }
    
    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }
    
    public boolean isRequireAuth() {
        return requireAuth;
    }
    
    public void setRequireAuth(boolean requireAuth) {
        this.requireAuth = requireAuth;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "EmailConfig{" +
                "id=" + id +
                ", smtpHost='" + smtpHost + '\'' +
                ", smtpPort=" + smtpPort +
                ", senderEmail='" + senderEmail + '\'' +
                ", senderName='" + senderName + '\'' +
                ", useTls=" + useTls +
                ", useSsl=" + useSsl +
                ", requireAuth=" + requireAuth +
                ", active=" + active +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailConfig that = (EmailConfig) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
