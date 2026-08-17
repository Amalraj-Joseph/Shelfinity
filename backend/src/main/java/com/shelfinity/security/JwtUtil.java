/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for JWT token operations using MicroProfile JWT.
 */
@ApplicationScoped
public class JwtUtil {
    
    @Inject
    private JsonWebToken jwt;

    /**
     * Get the current authenticated user's email from JWT token.
     */
    public Optional<String> getCurrentUserEmail() {
        if (jwt != null && jwt.getClaim(Claims.email) != null) {
            return Optional.of(jwt.getClaim(Claims.email));
        }
        return Optional.empty();
    }
    
    /**
     * Get the current authenticated user's Keycloak ID from JWT token.
     */
    public Optional<String> getCurrentUserKeycloakId() {
        if (jwt != null && jwt.getSubject() != null) {
            return Optional.of(jwt.getSubject());
        }
        return Optional.empty();
    }
    
    /**
     * Get the current authenticated user's role from JWT token.
     * Keycloak puts realm roles under the nested "realm_access.roles" claim
     * rather than the flat "groups" claim JsonWebToken#getGroups() reads, so
     * that claim has to be unpacked directly.
     */
    public Optional<String> getCurrentUserRole() {
        if (jwt == null) {
            return Optional.empty();
        }
        Object realmAccess = jwt.getClaim("realm_access");
        Collection<?> roles = null;
        if (realmAccess instanceof JsonObject) {
            roles = ((JsonObject) realmAccess).getJsonArray("roles");
        } else if (realmAccess instanceof Map) {
            Object rolesClaim = ((Map<?, ?>) realmAccess).get("roles");
            if (rolesClaim instanceof Collection) {
                roles = (Collection<?>) rolesClaim;
            }
        }
        if (roles != null) {
            // Keycloak may include other realm roles (e.g. offline_access,
            // uma_authorization) alongside ours, so look for a known
            // application role rather than assuming array order/position.
            for (Object roleValue : roles) {
                String role = roleValue instanceof JsonString
                        ? ((JsonString) roleValue).getString()
                        : String.valueOf(roleValue);
                if ("admin".equals(role) || "user".equals(role)) {
                    return Optional.of(role);
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * Check if the current user has admin role.
     */
    public boolean isCurrentUserAdmin() {
        return getCurrentUserRole()
                .map(role -> "admin".equals(role))
                .orElse(false);
    }
    
    /**
     * Check if the current user has a specific role.
     */
    public boolean hasRole(String role) {
        return getCurrentUserRole()
                .map(userRole -> role.equals(userRole))
                .orElse(false);
    }
    
    /**
     * Check if the current user is authenticated.
     * Deliberately checks the injected JsonWebToken rather than
     * SecurityContext#getUserPrincipal(): @Context-injected SecurityContext
     * is not reliably populated in an @ApplicationScoped bean, which made
     * this always return false even for validly authenticated requests.
     */
    public boolean isAuthenticated() {
        return jwt != null && jwt.getSubject() != null;
    }
    
    /**
     * Get user information from the current JWT token.
     */
    public Optional<UserInfo> getCurrentUserInfo() {
        if (!isAuthenticated()) {
            return Optional.empty();
        }
        
        UserInfo userInfo = new UserInfo();
        userInfo.setKeycloakId(getCurrentUserKeycloakId().orElse(null));
        userInfo.setEmail(getCurrentUserEmail().orElse(null));
        userInfo.setRole(getCurrentUserRole().orElse(null));
        
        return Optional.of(userInfo);
    }
    
    /**
     * User information extracted from JWT token.
     */
    public static class UserInfo {
        private String keycloakId;
        private String email;
        private String role;
        
        public String getKeycloakId() {
            return keycloakId;
        }
        
        public void setKeycloakId(String keycloakId) {
            this.keycloakId = keycloakId;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        @Override
        public String toString() {
            return "UserInfo{" +
                    "keycloakId='" + keycloakId + '\'' +
                    ", email='" + email + '\'' +
                    ", role='" + role + '\'' +
                    '}';
        }
    }
}
