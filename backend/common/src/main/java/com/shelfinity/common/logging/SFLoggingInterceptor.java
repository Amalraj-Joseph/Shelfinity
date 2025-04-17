/*
 * MIT License
 * 
 * Copyright (c) 2025 Shadow-Codex
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.shelfinity.common.logging;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Interceptor that logs method entry, exit, and exception details.
 * <p>
 * This interceptor is activated by the {@link SFLoggable} annotation. It
 * logs the method name, class name, execution time, and any exceptions
 * that are thrown during method execution.
 * </p>
 */
@SFLoggable
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class SFLoggingInterceptor {

    @Inject 
    private SFLogger logger;
    /**
     * Intercepts method invocation to log the method entry, exit, execution time, and any exceptions thrown.
     * 
     * @param ctx The invocation context
     * @return The result of the method invocation
     * @throws Exception If an error occurs during method invocation
     */
    @AroundInvoke
    public Object logMethodEntryExit(InvocationContext ctx) throws Exception {
        String className = ctx.getMethod().getDeclaringClass().getName();
        String methodName = ctx.getMethod().getName();

        // Log method entry with INFO level
        logger.info(className + "." + methodName + " started");

        long start = System.currentTimeMillis();

        try {
            // Proceed with the method execution
            Object result = ctx.proceed();
            
            // Log method exit with execution time (INFO level)
            long duration = System.currentTimeMillis() - start;
            logger.info(className + "." + methodName + " exited [Execution time: " + duration + " ms]");
            
            return result;
        } catch (Exception ex) {
            // Log exception with SEVERE level
            logger.severe(className + "." + methodName + " failed: " + ex.getMessage());
            throw ex;
        }
    }
}
