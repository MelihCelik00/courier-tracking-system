package com.melihcelik.couriertracking.infrastructure.rest;

import com.melihcelik.couriertracking.application.query.CourierQueryService;
import com.melihcelik.couriertracking.domain.event.CourierLocationEvent;
import com.melihcelik.couriertracking.domain.model.Courier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierControllerTest {

    @Mock
    private CourierQueryService queryService;

    @Mock
    private KafkaTemplate<String, CourierLocationEvent> kafkaTemplate;

    @Captor
    private ArgumentCaptor<CourierLocationEvent> eventCaptor;

    private CourierController controller;

    @BeforeEach
    void setUp() {
        controller = new CourierController(queryService, kafkaTemplate);
    }

    @Test
    void reportLocation_ValidInput_ShouldPublishEventAndReturnAccepted() {
        // Arrange
        Long courierId = 1L;
        Double latitude = 40.9923307;
        Double longitude = 29.1244229;
        SendResult<String, CourierLocationEvent> mockSendResult = mock(SendResult.class);
        when(kafkaTemplate.send(eq("courier.location"), any(CourierLocationEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(mockSendResult));

        // Act
        ResponseEntity<Void> response = controller.reportLocation(courierId, latitude, longitude);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(kafkaTemplate).send(eq("courier.location"), eventCaptor.capture());
        CourierLocationEvent capturedEvent = eventCaptor.getValue();
        assertEquals(courierId, capturedEvent.getCourierId());
        assertEquals(latitude, capturedEvent.getLatitude());
        assertEquals(longitude, capturedEvent.getLongitude());
        assertNotNull(capturedEvent.getTimestamp());
    }

    @Test
    void getCourier_ExistingCourier_ShouldReturnCourier() {
        // Arrange
        Long courierId = 1L;
        Courier courier = Courier.builder()
                .id(courierId)
                .totalTravelDistance(100.0)
                .lastLatitude(40.9923307)
                .lastLongitude(29.1244229)
                .isActive(true)
                .build();
        when(queryService.getCourierById(courierId)).thenReturn(Optional.of(courier));

        // Act
        ResponseEntity<Courier> response = controller.getCourier(courierId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(courierId, response.getBody().getId());
    }

    @Test
    void getCourier_NonExistingCourier_ShouldReturnNotFound() {
        // Arrange
        Long courierId = 1L;
        when(queryService.getCourierById(courierId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Courier> response = controller.getCourier(courierId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void reportLocation_KafkaFailure_ShouldStillReturnAccepted() {
        // Arrange
        Long courierId = 1L;
        Double latitude = 40.9923307;
        Double longitude = 29.1244229;
        CompletableFuture<SendResult<String, CourierLocationEvent>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(eq("courier.location"), any(CourierLocationEvent.class)))
                .thenReturn(future);

        // Act
        ResponseEntity<Void> response = controller.reportLocation(courierId, latitude, longitude);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(kafkaTemplate).send(eq("courier.location"), any(CourierLocationEvent.class));
    }

    @Test
    void getTotalTravelDistance_ShouldReturnDistance() {
        // Arrange
        Long courierId = 1L;
        double expectedDistance = 150.5;
        when(queryService.getTotalTravelDistance(courierId)).thenReturn(expectedDistance);

        // Act
        ResponseEntity<Double> response = controller.getTotalTravelDistance(courierId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedDistance, response.getBody());
        verify(queryService).getTotalTravelDistance(courierId);
    }

    @Test
    void getTotalTravelDistance_NonExistingCourier_ShouldReturnZero() {
        // Arrange
        Long courierId = 999L;
        when(queryService.getTotalTravelDistance(courierId)).thenReturn(0.0);

        // Act
        ResponseEntity<Double> response = controller.getTotalTravelDistance(courierId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0.0, response.getBody());
        verify(queryService).getTotalTravelDistance(courierId);
    }
} 