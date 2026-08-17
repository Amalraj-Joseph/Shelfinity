/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservations;

/**
 * Enum representing the status of a book reservation.
 */
public enum ReservationStatus {
    /**
     * Reservation is active and waiting for the book to become available.
     */
    ACTIVE,
    
    /**
     * User has been notified that the book is available.
     */
    NOTIFIED,
    
    /**
     * Reservation has been fulfilled (user borrowed the book).
     */
    FULFILLED,
    
    /**
     * Reservation was cancelled by the user.
     */
    CANCELLED,
    
    /**
     * Reservation expired without being fulfilled.
     */
    EXPIRED
}
