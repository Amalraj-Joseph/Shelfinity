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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Shelfinity Logger
 */
public class SFLogger {

    private static final Logger logger = Logger.getLogger(SFLogger.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    static {
        // Clear default handlers
        LogManager.getLogManager().reset();

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord record) {
                return String.format("[%s] %s %s - %s%n",
                        record.getLevel().getName(),
                        LocalDateTime.now().format(FORMATTER),
                        record.getLoggerName(),
                        record.getMessage());
            }
        });

        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }

    public SFLogger(){
        // Default Constructor
    }

    /**
     * Log a message at INFO level.
     * @param message the message to log
     */
    public void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Log a message at SEVERE level.
     * @param message the message to log
     */
    public void severe(String message) {
        log(Level.SEVERE, message);
    }

    /**
     * Log a message at WARNING level.
     * @param message the message to log
     */
    public void warning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Log a message at FINE level.
     * @param message the message to log
     */
    public void fine(String message) {
        log(Level.FINE, message);
    }

    /**
     * Log a message at CONFIG level.
     * @param message the message to log
     */
    public void config(String message) {
        log(Level.CONFIG, message);
    }

    /**
     * Log a message at FINEST level.
     * @param message the message to log
     */
    public void finest(String message) {
        log(Level.FINEST, message);
    }

    /**
     * Log a message at OFF level (no logging).
     * @param message the message to log
     */
    public void off(String message) {
        log(Level.OFF, message);
    }

    /**
     * Log a message with the given level.
     * @param level the log level
     * @param message the message to log
     */
    public static void log(Level level, String message) {
        if (logger.isLoggable(level)) {
            StackTraceElement caller = findCaller();
            String context = caller.getClassName() + "." + caller.getMethodName();
            logger.log(level, context + " - " + message);
        }
    }

    /**
     * Find the caller of the log method.
     * @return the caller's stack trace element
     */
    private static StackTraceElement findCaller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stack.length; i++) {
            String className = stack[i].getClassName();
            if (!className.equals(SFLogger.class.getName()) &&
                !className.startsWith("java.lang.Thread")) {
                return stack[i];
            }
        }
        return stack[2]; // fallback
    }
}
