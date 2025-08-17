/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.common.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Shelfinity Logger
 */
@ApplicationScoped
public class SFLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private static Logger getLogger(String className) {
        Logger logger = Logger.getLogger(className);
        // Clear default handlers
        LogManager.getLogManager().reset();
        ConsoleHandler handler = new ConsoleHandler() {
            @Override
            protected synchronized void setOutputStream(java.io.OutputStream out) throws SecurityException {
                super.setOutputStream(System.out); // Force to stdout
            }
        };
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord record) {
                StringBuilder sb = new StringBuilder();
                String level = String.format("[%-8s]", record.getLevel().getName());

                sb.append(String.format("%s %s %s.%s %s%n",
                        level,
                        LocalDateTime.now().format(FORMATTER),
                        record.getSourceClassName(),
                        record.getSourceMethodName(),
                        record.getMessage()));

                if (record.getThrown() != null) {
                    Throwable t = record.getThrown();
                    sb.append(getStackTrace(t));
                }
                return sb.toString();
            }

        });
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        return logger;
    }

    /**
     * Log a message at INFO level.
     *
     * @param message the message to log
     */
    public void info(String className, String methodName, String message) {
        log(Level.INFO, className, methodName, message);
    }

    /**
     * Log a message at SEVERE level.
     *
     * @param message the message to log
     */
    public void severe(String className, String methodName, String message, Throwable t) {
        log(Level.SEVERE, className, methodName, message, t);
    }

    /**
     * Log a message at WARNING level.
     *
     * @param message the message to log
     */
    public void warning(String className, String methodName, String message) {
        log(Level.WARNING, className, methodName, message);
    }

    /**
     * Log a message at FINE level.
     *
     * @param message the message to log
     */
    public void fine(String className, String methodName, String message) {
        log(Level.FINE, className, methodName, message);
    }

    /**
     * Log a message at CONFIG level.
     *
     * @param message the message to log
     */
    public void config(String className, String methodName, String message) {
        log(Level.CONFIG, className, methodName, message);
    }

    /**
     * Log a message at FINEST level.
     *
     * @param message the message to log
     */
    public void finest(String className, String methodName, String message) {
        log(Level.FINEST, className, methodName, message);
    }

    /**
     * Log a message at OFF level (no logging).
     *
     * @param message the message to log
     */
    public void off(String className, String methodName, String message) {
        log(Level.OFF, className, methodName, message);
    }

    /**
     * Log a message with the given level.
     *
     * @param level the log level
     * @param message the message to log
     */
    public static void log(Level level, String className, String methodName, String message) {
        Logger logger = getLogger(className);
        if (logger.isLoggable(level)) {
            logger.logp(level, className, methodName, String.format("%s", message));
        }
    }

    /**
     * Log a message with the given level.
     *
     * @param level the log level
     * @param message the message to log
     */
    public static void log(Level level, String className, String methodName, String message, Throwable t) {
        Logger logger = getLogger(className);
        logger.logp(level, className, methodName, String.format("%s", message), t);
    }

    private static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);

        String[] lines = sw.toString().split(System.lineSeparator());
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append("\t").append(line).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
