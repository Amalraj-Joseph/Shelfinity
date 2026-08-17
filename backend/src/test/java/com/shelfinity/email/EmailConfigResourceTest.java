/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.email.dto.EmailConfigResponse;
import com.shelfinity.security.JwtUtil;

import jakarta.ws.rs.core.Response;

/**
 * SPEC.md §10.6 (resolved): every read path must return {@link EmailConfigResponse},
 * never the raw {@link EmailConfig} entity (which would leak the encrypted
 * password field name/value), and a PUT that omits a password must not clear
 * the existing one.
 */
@ExtendWith(MockitoExtension.class)
class EmailConfigResourceTest {

    @Mock private EmailConfigRepository emailConfigRepository;
    @Mock private EmailService emailService;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private EmailConfigResource emailConfigResource;

    private static EmailConfig configWithPassword(String password) {
        EmailConfig config = new EmailConfig("smtp.example.com", 587, "noreply@shelfinity.com");
        config.setPassword(password);
        return config;
    }

    @Test
    void saveEmailConfig_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = emailConfigResource.saveEmailConfig(configWithPassword("secret"));

        assertThat(response.getStatus()).isEqualTo(403);
        verify(emailConfigRepository, never()).save(any());
    }

    @Test
    void saveEmailConfig_admin_returnsResponseDtoNotRawEntity() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        EmailConfig saved = configWithPassword("secret");
        when(emailConfigRepository.save(any(EmailConfig.class))).thenReturn(saved);

        Response response = emailConfigResource.saveEmailConfig(configWithPassword("secret"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(EmailConfigResponse.class);
        verify(emailService).refreshMailSession();
    }

    @Test
    void getActiveConfig_returnsResponseDtoWithoutPasswordField() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(emailConfigRepository.findActiveConfig()).thenReturn(Optional.of(configWithPassword("secret")));

        Response response = emailConfigResource.getActiveConfig();

        assertThat(response.getEntity()).isInstanceOf(EmailConfigResponse.class);
        EmailConfigResponse body = (EmailConfigResponse) response.getEntity();
        assertThat(body.isHasPassword()).isTrue();
    }

    @Test
    void getActiveConfig_none_returns404() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(emailConfigRepository.findActiveConfig()).thenReturn(Optional.empty());

        Response response = emailConfigResource.getActiveConfig();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void updateConfig_requestOmitsPassword_preservesExistingEncryptedPassword() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        EmailConfig existing = configWithPassword("existing-encrypted-value");
        existing.setId(id);
        when(emailConfigRepository.findById(id)).thenReturn(Optional.of(existing));
        when(emailConfigRepository.update(any(EmailConfig.class))).thenAnswer(inv -> inv.getArgument(0));

        // Simulates a typical edit-then-submit UI flow: GET never returned a
        // password, so the PUT body has none either.
        EmailConfig updateRequest = new EmailConfig("smtp.newhost.com", 465, "noreply@shelfinity.com");

        emailConfigResource.updateConfig(id.toString(), updateRequest);

        ArgumentCaptor<EmailConfig> captor = ArgumentCaptor.forClass(EmailConfig.class);
        verify(emailConfigRepository).update(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("existing-encrypted-value");
        assertThat(captor.getValue().getSmtpHost()).isEqualTo("smtp.newhost.com");
    }

    @Test
    void updateConfig_requestSuppliesNewPassword_overridesExisting() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        EmailConfig existing = configWithPassword("old-password");
        existing.setId(id);
        when(emailConfigRepository.findById(id)).thenReturn(Optional.of(existing));
        when(emailConfigRepository.update(any(EmailConfig.class))).thenAnswer(inv -> inv.getArgument(0));

        EmailConfig updateRequest = configWithPassword("brand-new-password");

        emailConfigResource.updateConfig(id.toString(), updateRequest);

        ArgumentCaptor<EmailConfig> captor = ArgumentCaptor.forClass(EmailConfig.class);
        verify(emailConfigRepository).update(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("brand-new-password");
    }

    @Test
    void updateConfig_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(emailConfigRepository.findById(id)).thenReturn(Optional.empty());

        Response response = emailConfigResource.updateConfig(id.toString(), configWithPassword("x"));

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void deleteConfig_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = emailConfigResource.deleteConfig(UUID.randomUUID().toString());

        assertThat(response.getStatus()).isEqualTo(403);
        verify(emailConfigRepository, never()).deleteById(any());
    }

    @Test
    void testEmailConfig_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = emailConfigResource.testEmailConfig(new EmailConfigResource.TestEmailRequest());

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void getAllConfigs_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = emailConfigResource.getAllConfigs();

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void getAllConfigs_admin_returnsResponseDtos() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(emailConfigRepository.findAll()).thenReturn(java.util.List.of(configWithPassword("secret")));

        Response response = emailConfigResource.getAllConfigs();

        assertThat(response.getStatus()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        var body = (java.util.List<Object>) response.getEntity();
        assertThat(body).hasSize(1).allSatisfy(entry -> assertThat(entry).isInstanceOf(EmailConfigResponse.class));
    }

    @Test
    void activateConfig_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = emailConfigResource.activateConfig(UUID.randomUUID().toString());

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void activateConfig_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(emailConfigRepository.findById(id)).thenReturn(Optional.empty());

        Response response = emailConfigResource.activateConfig(id.toString());

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void activateConfig_admin_activatesAndRefreshesMailSession() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(emailConfigRepository.findById(id)).thenReturn(Optional.of(configWithPassword("secret")));

        Response response = emailConfigResource.activateConfig(id.toString());

        assertThat(response.getStatus()).isEqualTo(200);
        verify(emailConfigRepository).activate(id);
        verify(emailService).refreshMailSession();
    }

    @Test
    void deleteConfig_admin_deletesAndReturns204() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);

        Response response = emailConfigResource.deleteConfig(id.toString());

        assertThat(response.getStatus()).isEqualTo(204);
        verify(emailConfigRepository).deleteById(id);
    }

    @Test
    void testEmailConfig_admin_success() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(emailService.sendEmail(org.mockito.ArgumentMatchers.eq("test@b.com"),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(true);
        EmailConfigResource.TestEmailRequest request = new EmailConfigResource.TestEmailRequest();
        request.setTo("test@b.com");

        Response response = emailConfigResource.testEmailConfig(request);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void testEmailConfig_admin_failureReturns500() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(emailService.sendEmail(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(false);
        EmailConfigResource.TestEmailRequest request = new EmailConfigResource.TestEmailRequest();
        request.setTo("test@b.com");

        Response response = emailConfigResource.testEmailConfig(request);

        assertThat(response.getStatus()).isEqualTo(500);
    }
}
