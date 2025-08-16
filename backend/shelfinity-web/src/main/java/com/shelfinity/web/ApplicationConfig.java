/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.web;

import java.util.Set;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import com.shelfinity.admin.api.impl.QueueImpl;
import com.shelfinity.user.api.impl.UserImpl;
import com.shelfinity.user.api.mapper.DataBaseExceptionMapper;
import com.shelfinity.user.api.mapper.UnauthorizedExceptionMapper;
import com.shelfinity.user.api.mapper.UserAlreadyExistsExceptionMapper;
import com.shelfinity.user.api.mapper.UserNotExistsExceptionMapper;
import com.shelfinity.user.api.mapper.UserServiceExceptionMapper;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/") // Context root is "/"
@OpenAPIDefinition(
    info = @Info(
        title = "Shelfinity Library Management System (LMS) REST APIs",
        version = "1.0"
    )
)
public class ApplicationConfig extends Application {

    private static final Set<Class<?>> applicationClasses = Set.of(
        UserImpl.class,
        QueueImpl.class,
        
        UserAlreadyExistsExceptionMapper.class,
        UserServiceExceptionMapper.class,
        DataBaseExceptionMapper.class,
        UnauthorizedExceptionMapper.class,
        UserNotExistsExceptionMapper.class
    );

    @Override
    public Set<Class<?>> getClasses() {
        return applicationClasses;
    }
}
