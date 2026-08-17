/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.email.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.shelfinity.email.EmailConfig;

/**
 * Response DTO for email configuration — deliberately omits {@code password}.
 * SPEC.md §10.6 (resolved) / api.yaml {@code EmailConfig.password} is
 * {@code writeOnly}; the entity itself must never be serialized directly.
 */
public class EmailConfigResponse {

    private UUID id;
    private String smtpHost;
    private int smtpPort;
    private String senderEmail;
    private String senderName;
    private String username;
    private boolean hasPassword;
    private boolean useTls;
    private boolean useSsl;
    private boolean requireAuth;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmailConfigResponse() {}

    public EmailConfigResponse(EmailConfig config) {
        this.id = config.getId();
        this.smtpHost = config.getSmtpHost();
        this.smtpPort = config.getSmtpPort();
        this.senderEmail = config.getSenderEmail();
        this.senderName = config.getSenderName();
        this.username = config.getUsername();
        this.hasPassword = config.getPassword() != null && !config.getPassword().isEmpty();
        this.useTls = config.isUseTls();
        this.useSsl = config.isUseSsl();
        this.requireAuth = config.isRequireAuth();
        this.active = config.isActive();
        this.createdAt = config.getCreatedAt();
        this.updatedAt = config.getUpdatedAt();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }

    public int getSmtpPort() { return smtpPort; }
    public void setSmtpPort(int smtpPort) { this.smtpPort = smtpPort; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isHasPassword() { return hasPassword; }
    public void setHasPassword(boolean hasPassword) { this.hasPassword = hasPassword; }

    public boolean isUseTls() { return useTls; }
    public void setUseTls(boolean useTls) { this.useTls = useTls; }

    public boolean isUseSsl() { return useSsl; }
    public void setUseSsl(boolean useSsl) { this.useSsl = useSsl; }

    public boolean isRequireAuth() { return requireAuth; }
    public void setRequireAuth(boolean requireAuth) { this.requireAuth = requireAuth; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
