package com.paypal.wallet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Catches any unhandled exception across all controllers and returns
 * a 400 Bad Request with the exception message instead of a generic 500.
 * This prevents blank "error 500" responses and makes debugging easier.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAll(Exception ex) {
        ex.printStackTrace(); // Prints full stack trace to server console
        return ResponseEntity
                .badRequest()
                .body("Server error: " + ex.getClass().getSimpleName() + " — " + ex.getMessage());
    }
}
