/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.auth;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.shelfinity.security.JwtUtil;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;
import com.shelfinity.users.dto.responses.UserResponse;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API for authentication operations.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Tag(name = "Authentication")
public class AuthResource {

    private static final Logger LOGGER = Logger.getLogger(AuthResource.class.getName());
    private static final HttpClient OIDC_HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Inject
    private JwtUtil jwtUtil;

    @Inject
    private UserRepository userRepository;

    /**
     * Login endpoint - validates JWT token and returns user info.
     * The actual authentication is done by Keycloak, this endpoint
     * just validates the token and returns user information.
     */
    @POST
    @Path("/login")
    @Tag(name = "Authentication")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Login with JWT token",
        description = "Validates the JWT token from Keycloak and returns user information"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "Authentication failed",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Authentication required\"}")
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found in system",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"User not found\"}")
            )
        )
    })
    public Response login() {
        // Get user info from JWT token
        Optional<JwtUtil.UserInfo> userInfo = jwtUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Authentication required\"}")
                    .build();
        }
        
        // Find user in database by Keycloak ID
        Optional<User> user = userRepository.findByKeycloakId(userInfo.get().getKeycloakId());
        if (user.isEmpty()) {
            // User authenticated in Keycloak but not in our system
            // This could happen for new users
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"User not found in system. Please contact administrator.\"}")
                    .build();
        }
        
        UserResponse response = new UserResponse(user.get());
        return Response.ok(response).build();
    }
    
    /**
     * Validate token endpoint - checks if the current JWT token is valid.
     */
    @GET
    @Path("/validate")
    @Tag(name = "Authentication")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Validate JWT token",
        description = "Validates the current JWT token and returns user information if valid"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Token is valid",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "Token is invalid or expired",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Invalid or expired token\"}")
            )
        )
    })
    public Response validateToken() {
        // Check if user is authenticated
        if (!jwtUtil.isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Invalid or expired token\"}")
                    .build();
        }
        
        // Get user info from JWT token
        Optional<JwtUtil.UserInfo> userInfo = jwtUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Invalid or expired token\"}")
                    .build();
        }
        
        // Find user in database
        Optional<User> user = userRepository.findByKeycloakId(userInfo.get().getKeycloakId());
        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"User not found in system\"}")
                    .build();
        }
        
        UserResponse response = new UserResponse(user.get());
        return Response.ok(response).build();
    }
    
    /**
     * Get current user profile.
     */
    @GET
    @Path("/me")
    @Tag(name = "Authentication")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Get current user profile",
        description = "Returns the profile of the currently authenticated user"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Authentication required\"}")
            )
        )
    })
    public Response getCurrentUser() {
        Optional<JwtUtil.UserInfo> userInfo = jwtUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Authentication required\"}")
                    .build();
        }
        
        Optional<User> user = userRepository.findByKeycloakId(userInfo.get().getKeycloakId());
        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"User not found\"}")
                    .build();
        }
        
        UserResponse response = new UserResponse(user.get());
        return Response.ok(response).build();
    }

    /**
     * Server-side Authorization Code + PKCE token exchange, for OIDC
     * providers (IBM Cloud App ID) whose token endpoint doesn't send CORS
     * headers — confirmed by hand against a real App ID tenant: the
     * browser's direct fetch() to App ID's token endpoint was blocked with
     * "CORS header 'Access-Control-Allow-Origin' missing". Server-to-server
     * calls aren't subject to CORS at all, so relaying the exchange through
     * here sidesteps that regardless of client type.
     *
     * Also required for a second, unrelated reason found the same way: App
     * ID rejects this exchange with "invalid_client...please provide
     * clientId as username and secret as password" unless the request
     * carries an Authorization: Basic header — but per App ID's own client
     * SDK (appid-clientsdk-js, retrieveTokens()), that's not a registered
     * secret (there isn't one for a Single-Page-Application registration);
     * it's a non-standard App ID convention where the PKCE code_verifier
     * itself fills the password slot. Doesn't change anything about where
     * this belongs — the code_verifier already has to reach this endpoint
     * either way — but it's why the request below sends it twice (form body
     * and Basic-Auth header).
     *
     * Also resolves the profile (email/name) via the UserInfo endpoint in
     * the same round trip: App ID's tokens carry no email/name claims at
     * all (confirmed via its discovery document's claims_supported), so the
     * frontend would otherwise need a second cross-origin call here too.
     *
     * Only reachable/meaningful when the frontend is built in pkce mode
     * (see AuthContext.js's AUTH_FLOW) — local Keycloak/ropc deployments
     * never call this endpoint, and OIDC_TOKEN_ENDPOINT/OIDC_CLIENT_ID are
     * simply unset there.
     */
    @POST
    @Path("/oidc-exchange")
    @Tag(name = "Authentication")
    @Operation(
        summary = "Exchange an OIDC authorization code for a token",
        description = "Server-side PKCE code exchange + profile lookup, for providers whose token endpoint doesn't support direct browser calls"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Exchange successful"),
        @APIResponse(responseCode = "401", description = "The authorization code or PKCE verifier was rejected by the provider"),
        @APIResponse(responseCode = "500", description = "OIDC provider not configured, or the exchange request itself failed")
    })
    public Response oidcExchange(OidcExchangeRequest request) {
        Optional<String> tokenEndpoint = ConfigProvider.getConfig().getOptionalValue("oidc.token.endpoint", String.class);
        Optional<String> clientId = ConfigProvider.getConfig().getOptionalValue("oidc.client.id", String.class);
        if (tokenEndpoint.isEmpty() || clientId.isEmpty()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"OIDC token exchange is not configured\"}")
                    .build();
        }
        try {
            String form = "grant_type=authorization_code"
                    + "&redirect_uri=" + URLEncoder.encode(request.getRedirectUri(), StandardCharsets.UTF_8)
                    + "&code=" + URLEncoder.encode(request.getCode(), StandardCharsets.UTF_8)
                    + "&code_verifier=" + URLEncoder.encode(request.getCodeVerifier(), StandardCharsets.UTF_8);

            // App ID's own client SDK (appid-clientsdk-js, retrieveTokens()) is
            // the source for this: it's not a registered client secret — there
            // isn't one for a Single-Page-Application registration — it's a
            // non-standard App ID convention where the Basic-Auth "password"
            // slot is filled with the PKCE code_verifier itself. Confirmed by
            // hand against a real App ID tenant: sending code_verifier only in
            // the body (the usual PKCE contract) got "invalid_client...please
            // provide clientId as username and secret as password"; this
            // matches the SDK's own request shape and works.
            String basicAuth = Base64.getEncoder().encodeToString(
                    (clientId.get() + ":" + request.getCodeVerifier()).getBytes(StandardCharsets.UTF_8));
            HttpRequest tokenHttpRequest = HttpRequest.newBuilder(URI.create(tokenEndpoint.get()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + basicAuth)
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            HttpResponse<String> tokenHttpResponse = OIDC_HTTP_CLIENT.send(tokenHttpRequest, HttpResponse.BodyHandlers.ofString());
            if (tokenHttpResponse.statusCode() != 200) {
                LOGGER.warning("OIDC token exchange rejected: " + tokenHttpResponse.statusCode() + " " + tokenHttpResponse.body());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Sign in failed\"}")
                        .build();
            }
            JsonObject tokenJson = Json.createReader(new java.io.StringReader(tokenHttpResponse.body())).readObject();
            String accessToken = tokenJson.getString("access_token");

            OidcExchangeResponse result = new OidcExchangeResponse();
            result.setAccessToken(accessToken);

            Optional<String> userinfoEndpoint = ConfigProvider.getConfig().getOptionalValue("oidc.userinfo.endpoint", String.class);
            if (userinfoEndpoint.isPresent()) {
                HttpRequest userinfoHttpRequest = HttpRequest.newBuilder(URI.create(userinfoEndpoint.get()))
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build();
                HttpResponse<String> userinfoHttpResponse = OIDC_HTTP_CLIENT.send(userinfoHttpRequest, HttpResponse.BodyHandlers.ofString());
                if (userinfoHttpResponse.statusCode() == 200) {
                    JsonObject profile = Json.createReader(new java.io.StringReader(userinfoHttpResponse.body())).readObject();
                    result.setSub(profile.getString("sub", null));
                    result.setEmail(profile.getString("email", null));
                    result.setName(profile.getString("name", null));
                } else {
                    LOGGER.warning("OIDC userinfo lookup failed: " + userinfoHttpResponse.statusCode() + " " + userinfoHttpResponse.body());
                }
            }

            return Response.ok(result).build();
        } catch (Exception e) {
            LOGGER.severe("OIDC exchange error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Sign in failed\"}")
                    .build();
        }
    }

    public static class OidcExchangeRequest {
        private String code;
        private String codeVerifier;
        private String redirectUri;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getCodeVerifier() { return codeVerifier; }
        public void setCodeVerifier(String codeVerifier) { this.codeVerifier = codeVerifier; }
        public String getRedirectUri() { return redirectUri; }
        public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
    }

    public static class OidcExchangeResponse {
        private String accessToken;
        private String sub;
        private String email;
        private String name;

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getSub() { return sub; }
        public void setSub(String sub) { this.sub = sub; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
