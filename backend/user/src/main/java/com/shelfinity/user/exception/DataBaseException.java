/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.exception;

public class DataBaseException extends RuntimeException {
    
    public DataBaseException(){
        super();
    }
    
    public DataBaseException(String message) {
        super(message);
    }
}