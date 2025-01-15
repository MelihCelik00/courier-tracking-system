package com.melihcelik.couriertracking.infrastructure.rest.error;

import com.melihcelik.couriertracking.domain.exception.CourierNotFoundException;
import com.melihcelik.couriertracking.domain.exception.InvalidLocationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleCourierNotFoundException_ShouldReturnNotFound() {
        // Arrange
        CourierNotFoundException ex = new CourierNotFoundException(1L);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleCourierNotFoundException(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Courier not found with id: 1", errorResponse.getMessage());
        assertEquals("COURIER_NOT_FOUND", errorResponse.getCode());
    }

    @Test
    void handleInvalidLocationException_ShouldReturnBadRequest() {
        // Arrange
        InvalidLocationException ex = new InvalidLocationException("Invalid location");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleInvalidLocationException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Invalid location", errorResponse.getMessage());
        assertEquals("INVALID_LOCATION", errorResponse.getCode());
    }

    @Test
    void handleMissingParameterException_ShouldReturnBadRequest() {
        // Arrange
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("param", "String");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleMissingParameterException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Missing required parameter: param", errorResponse.getMessage());
        assertEquals("MISSING_PARAMETER", errorResponse.getCode());
    }

    @Test
    void handleTypeMismatchException_ShouldReturnBadRequest() {
        // Arrange
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("param");
        when(ex.getValue()).thenReturn("invalid");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleTypeMismatchException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Invalid value for parameter 'param': invalid", errorResponse.getMessage());
        assertEquals("INVALID_PARAMETER_TYPE", errorResponse.getCode());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        // Arrange
        Exception ex = new Exception("Unexpected error");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleGenericException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("An unexpected error occurred", errorResponse.getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", errorResponse.getCode());
    }
} 