package com.melihcelik.couriertracking.infrastructure.rest.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final String message;
    private final String code;
    private final Integer status;
    @Builder.Default
    private final Instant timestamp = Instant.now();
    private final List<ValidationError> errors;

    @Data
    @Builder
    public static class ValidationError {
        private final String field;
        private final String message;
    }
} 