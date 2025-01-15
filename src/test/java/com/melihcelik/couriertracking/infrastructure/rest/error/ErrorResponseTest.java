package com.melihcelik.couriertracking.infrastructure.rest.error;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void builder_ShouldCreateErrorResponse() {
        // Arrange
        String message = "Error message";
        String code = "ERROR_CODE";
        int status = 400;
        Instant timestamp = Instant.now();
        List<ErrorResponse.ValidationError> errors = List.of(
                ErrorResponse.ValidationError.builder().field("field1").message("error1").build(),
                ErrorResponse.ValidationError.builder().field("field2").message("error2").build()
        );

        // Act
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(message)
                .code(code)
                .status(status)
                .timestamp(timestamp)
                .errors(errors)
                .build();

        // Assert
        assertEquals(message, errorResponse.getMessage());
        assertEquals(code, errorResponse.getCode());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(errors, errorResponse.getErrors());
    }

    @Test
    void builder_ShouldCreateErrorResponseWithoutErrors() {
        // Arrange
        String message = "Error message";
        String code = "ERROR_CODE";
        int status = 400;

        // Act
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(message)
                .code(code)
                .status(status)
                .build();

        // Assert
        assertEquals(message, errorResponse.getMessage());
        assertEquals(code, errorResponse.getCode());
        assertEquals(status, errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        assertNull(errorResponse.getErrors());
    }
} 