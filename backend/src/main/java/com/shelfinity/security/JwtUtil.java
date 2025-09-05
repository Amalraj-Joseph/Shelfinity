package com.shelfinity.security;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JwtUtil {

    @Inject
    private JsonWebToken jwt;

    // Make them optional; no defaultValue = "" (which triggers the error if empty)
    @Inject
    @ConfigProperty(name = "security.jwt.expectedAudience", defaultValue = "___UNSET___")
    private String expectedAudienceRaw;

    @Inject
    @ConfigProperty(name = "security.jwt.expectedIssuer", defaultValue = "___UNSET___")
    private String expectedIssuerRaw;

    /* ----------------------------- Basic getters ----------------------------- */

    public boolean isAuthenticated() {
        return jwt != null && jwt.getSubject() != null;
    }

    public Optional<String> subject() {
        return (jwt == null) ? Optional.empty() : Optional.ofNullable(jwt.getSubject());
    }

    public Optional<String> email() {
        return (jwt == null) ? Optional.empty() : Optional.ofNullable(jwt.getClaim(Claims.email));
    }

    public Optional<String> preferredUsername() {
        return (jwt == null) ? Optional.empty() : Optional.ofNullable(jwt.getClaim(Claims.preferred_username));
    }

    public Optional<String> name() {
        if (jwt == null) return Optional.empty();
        // Read by literal key to avoid enum .name() confusion
        String n = jwt.getClaim("name");
        if (n != null && !n.isBlank()) return Optional.of(n);
        String pu = jwt.getClaim(Claims.preferred_username);
        if (pu != null && !pu.isBlank()) return Optional.of(pu);
        return Optional.ofNullable(jwt.getSubject());
    }

    public Set<String> groups() {
        return (jwt == null || jwt.getGroups() == null) ? Collections.emptySet() : jwt.getGroups();
    }

    /** MP-JWT typically exposes aud as a Set<String>. */
    public Optional<Set<String>> audience() {
        return (jwt == null) ? Optional.empty() : Optional.ofNullable(jwt.getAudience());
    }

    public Optional<String> issuer() {
        return (jwt == null) ? Optional.empty() : Optional.ofNullable(jwt.getIssuer());
    }

    /* --------------------------- Role / audience checks --------------------------- */

    public boolean hasRole(String role) {
        return groups().contains(role);
    }

    public boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) return false;
        Set<String> g = groups();
        for (String r : roles) if (g.contains(r)) return true;
        return false;
    }

    public boolean hasAllRoles(String... roles) {
        if (roles == null || roles.length == 0) return false;
        Set<String> g = groups();
        for (String r : roles) if (!g.contains(r)) return false;
        return true;
    }

    /** True if expectedAudience is unset OR token's aud contains it. */
    public boolean audienceAccepted() {
        String expectedAudience = normalize(expectedAudienceRaw);
        if (expectedAudience == null) return true;
        return audience().map(aud -> aud.contains(expectedAudience)).orElse(false);
    }

    /** True if expectedIssuer is unset OR token's iss equals it. */
    public boolean issuerAccepted() {
        String expectedIssuer = normalize(expectedIssuerRaw);
        if (expectedIssuer == null) return true;
        return issuer().map(expectedIssuer::equals).orElse(false);
    }

    /** Overall token suitability for the backend. */
    public boolean tokenAccepted() {
        return isAuthenticated() && audienceAccepted() && issuerAccepted();
    }

    /* ------------------------------ User snapshot ------------------------------ */

    public Optional<UserInfo> currentUserInfo() {
        if (!tokenAccepted()) return Optional.empty();

        UserInfo info = new UserInfo();
        info.keycloakId = subject().orElse(null);
        info.username   = preferredUsername().orElse(null);
        info.email      = email().orElse(null);
        info.groups     = groups();
        info.issuer     = issuer().orElse(null);
        info.audience   = audience().orElse(Collections.emptySet()).stream().toList();
        return Optional.of(info);
    }

    /* --------------------------------- Helpers --------------------------------- */

    private static String normalize(String raw) {
        if (raw == null) return null;
        if ("___UNSET___".equals(raw)) return null;
        String t = raw.trim();
        return t.isEmpty() ? null : t;
    }

    /* --------------------------------- DTO --------------------------------- */

    public static class UserInfo {
        private String keycloakId;
        private String username;
        private String email;
        private Set<String> groups = Collections.emptySet();
        private String issuer;
        private List<String> audience = List.of();

        public String getKeycloakId() { return keycloakId; }
        public String getUsername()   { return username; }
        public String getEmail()      { return email; }
        public Set<String> getGroups(){ return groups; }
        public String getIssuer()     { return issuer; }
        public List<String> getAudience() { return audience; }

        @Override
        public String toString() {
            return "UserInfo{" +
                "keycloakId='" + keycloakId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", groups=" + groups +
                ", issuer='" + issuer + '\'' +
                ", audience=" + audience +
                '}';
        }
    }
    // Back-compat for callers still using the old name
    public java.util.Optional<UserInfo> getCurrentUserInfo() {
        return currentUserInfo();
    }

    public boolean isCurrentUserAdmin(){
        return hasRole("admin");
    }
}
