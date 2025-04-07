package com.monolith.exception;

import com.monolith.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation exceptions.
     *
     * @param ex The validation exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Collect all validation errors
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn(errorMessage);
        ErrorResponse errorResponse = new ErrorResponse(
                BAD_REQUEST.value(),
                "Validation error: " + errorMessage
        );
        
        return new ResponseEntity<>(errorResponse, BAD_REQUEST);
    }

    /**
     * Handles insufficient exceptions.
     *
     * @param ex The exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler({InsufficientBuyingPowerException.class, InsufficientInventoryException.class})
    public ResponseEntity<ErrorResponse> handleInsufficientException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                BAD_REQUEST.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, BAD_REQUEST);
    }

    /**
     * Handles order not found exceptions.
     *
     * @param ex The exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                NOT_FOUND.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, NOT_FOUND);
    }

    /**
     * Handles illegal argument exceptions.
     *
     * @param ex The exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                BAD_REQUEST.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, BAD_REQUEST);
    }

    /**
     * Fallback handler for all other exceptions.
     *
     * @param ex The exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        String errMessage = "An unexpected error occurred: " + ex.getMessage();
        log.error(errMessage);
        ErrorResponse errorResponse = new ErrorResponse(INTERNAL_SERVER_ERROR.value(), errMessage);
        return new ResponseEntity<>(errorResponse, INTERNAL_SERVER_ERROR);
    }
}