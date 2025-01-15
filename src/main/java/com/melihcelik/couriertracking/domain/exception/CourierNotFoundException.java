package com.melihcelik.couriertracking.domain.exception;

public class CourierNotFoundException extends RuntimeException {
    public CourierNotFoundException(Long courierId) {
        super(String.format("Courier not found with id: %d", courierId));
    }
} 