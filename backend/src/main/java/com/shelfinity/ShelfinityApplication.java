/*
 * Copyright (c) 2025 Shadow-Codex
 * Licensed under the MIT License.
 */
package com.shelfinity;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(
    info = @Info(
        title = "Shelfinity API",
        version = "1.0.0",
        description = "A comprehensive library management system API",
        contact = @Contact(
            name = "Shelfinity Team",
            email = "support@shelfinity.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:9080/shelfinity-backend", description = "Development Server")
    }
)
@SecurityScheme(
    securitySchemeName = "JWT",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class ShelfinityApplication extends Application {
    
    public ShelfinityApplication() {
        System.out.println("ShelfinityApplication constructor called!");
    }
}
