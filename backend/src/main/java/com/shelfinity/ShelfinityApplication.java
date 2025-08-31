/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Main application class for Shelfinity Library Management System.
 */
@ApplicationPath("/api")
public class ShelfinityApplication extends Application {
    
    public ShelfinityApplication() {
        System.out.println("ShelfinityApplication constructor called!");
    }
    
    // Liberty will auto-detect and register all JAX-RS resources
}
