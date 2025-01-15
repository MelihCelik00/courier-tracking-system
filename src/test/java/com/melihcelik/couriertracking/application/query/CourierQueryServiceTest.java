package com.melihcelik.couriertracking.application.query;

import com.melihcelik.couriertracking.domain.model.Courier;
import com.melihcelik.couriertracking.domain.repository.CourierRepository;
import com.melihcelik.couriertracking.domain.repository.StoreEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierQueryServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private StoreEntryRepository storeEntryRepository;

    private CourierQueryService service;

    @BeforeEach
    void setUp() {
        service = new CourierQueryService(courierRepository, storeEntryRepository);
    }

    @Test
    void getCourierById_ExistingCourier_ShouldReturnCourier() {
        // Arrange
        Long courierId = 1L;
        Courier courier = Courier.builder()
                .id(courierId)
                .totalTravelDistance(100.0)
                .lastLatitude(40.9923307)
                .lastLongitude(29.1244229)
                .isActive(true)
                .build();
        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));

        // Act
        Optional<Courier> result = service.getCourierById(courierId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(courierId, result.get().getId());
        assertEquals(100.0, result.get().getTotalTravelDistance());
        verify(courierRepository).findById(courierId);
    }

    @Test
    void getCourierById_NonExistingCourier_ShouldReturnEmpty() {
        // Arrange
        Long courierId = 1L;
        when(courierRepository.findById(courierId)).thenReturn(Optional.empty());

        // Act
        Optional<Courier> result = service.getCourierById(courierId);

        // Assert
        assertFalse(result.isPresent());
        verify(courierRepository).findById(courierId);
    }

    @Test
    void getTotalTravelDistance_ExistingCourier_ShouldReturnDistance() {
        // Arrange
        Long courierId = 1L;
        double expectedDistance = 150.5;
        Courier courier = Courier.builder()
                .id(courierId)
                .totalTravelDistance(expectedDistance)
                .lastLatitude(40.9923307)
                .lastLongitude(29.1244229)
                .isActive(true)
                .build();
        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));

        // Act
        double result = service.getTotalTravelDistance(courierId);

        // Assert
        assertEquals(expectedDistance, result);
        verify(courierRepository).findById(courierId);
    }

    @Test
    void getTotalTravelDistance_NonExistingCourier_ShouldReturnZero() {
        // Arrange
        Long courierId = 1L;
        when(courierRepository.findById(courierId)).thenReturn(Optional.empty());

        // Act
        double result = service.getTotalTravelDistance(courierId);

        // Assert
        assertEquals(0.0, result);
        verify(courierRepository).findById(courierId);
    }
} 