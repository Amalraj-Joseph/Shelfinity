/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

/**
 * Utility class for JWT token operations.
 */
@ApplicationScoped
public class JwtUtil {
    
    // In a real application, this would be configured via environment variables
    private static final String SECRET_KEY = "your-secret-key-here-make-it-long-and-secure-for-production";
    private static final long EXPIRATION_TIME = 3600000; // 1 hour in milliseconds
    
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    
    /**
     * Generate a JWT token for a user.
     */
    public String generateToken(String keycloakId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
                .setSubject(keycloakId)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Verify and parse a JWT token.
     */
    public Optional<Claims> verifyToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Extract user information from JWT token.
     */
    public Optional<UserInfo> extractUserInfo(String token) {
        return verifyToken(token).map(claims -> {
            UserInfo userInfo = new UserInfo();
            userInfo.setKeycloakId(claims.getSubject());
            userInfo.setEmail(claims.get("email", String.class));
            userInfo.setRole(claims.get("role", String.class));
            return userInfo;
        });
    }
    
    /**
     * Check if a token is valid.
     */
    public boolean isTokenValid(String token) {
        return verifyToken(token).isPresent();
    }
    
    /**
     * Extract token from Authorization header.
     */
    public Optional<String> extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
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
