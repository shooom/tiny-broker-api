package com.monolith.exception;

public class InsufficientBuyingPowerException extends RuntimeException {
    public InsufficientBuyingPowerException(String message) {
        super(message);
    }
}