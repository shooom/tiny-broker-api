package com.monolith.exception;

/**
 * Exception thrown when an order is not found.
 * This is a runtime exception as it represents a client error (accessing a non-existent resource)
 * rather than an exceptional condition in the application.
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}