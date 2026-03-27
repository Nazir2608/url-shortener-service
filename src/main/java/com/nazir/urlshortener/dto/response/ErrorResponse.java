package com.nazir.urlshortener.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard API error response structure.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    LocalDateTime timestamp,
    List<ValidationError> validationErrors
) {
    /**
     * Primary constructor with timestamp and no validation errors.
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now(), null);
    }

    /**
     * Constructor for validation errors.
     */
    public ErrorResponse(int status, String error, String message, String path, List<ValidationError> validationErrors) {
        this(status, error, message, path, LocalDateTime.now(), validationErrors);
    }

    /**
     * Represents a single field validation error.
     */
    public record ValidationError(
        String field,
        String message,
        Object rejectedValue
    ) {}
}
