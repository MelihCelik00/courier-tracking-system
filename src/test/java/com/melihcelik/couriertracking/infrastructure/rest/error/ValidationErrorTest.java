package com.melihcelik.couriertracking.infrastructure.rest.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationErrorTest {

    @Test
    void builder_ShouldCreateValidationError() {
        // Arrange
        String field = "field1";
        String message = "error1";

        // Act
        ErrorResponse.ValidationError validationError = ErrorResponse.ValidationError.builder()
                .field(field)
                .message(message)
                .build();

        // Assert
        assertEquals(field, validationError.getField());
        assertEquals(message, validationError.getMessage());
    }
}
