package com.melihcelik.couriertracking.application.command;

import com.melihcelik.couriertracking.domain.event.StoreEntryEvent;
import com.melihcelik.couriertracking.domain.model.Courier;
import com.melihcelik.couriertracking.domain.model.Store;
import com.melihcelik.couriertracking.domain.model.StoreEntry;
import com.melihcelik.couriertracking.domain.repository.CourierRepository;
import com.melihcelik.couriertracking.domain.repository.StoreEntryRepository;
import com.melihcelik.couriertracking.domain.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreEntryCommandServiceTest {

    @Mock
    private StoreEntryRepository storeEntryRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private StoreRepository storeRepository;

    @Captor
    private ArgumentCaptor<StoreEntry> storeEntryCaptor;

    private StoreEntryCommandService service;

    private static final int STORE_ENTRY_COOLDOWN = 60; // seconds

    @BeforeEach
    void setUp() {
        service = new StoreEntryCommandService(
                storeEntryRepository,
                courierRepository,
                storeRepository
        );
        ReflectionTestUtils.setField(service, "storeEntryCooldown", STORE_ENTRY_COOLDOWN);
    }

    @Test
    void processStoreEntry_ValidEntry_ShouldSaveEntry() {
        // Arrange
        Long courierId = 1L;
        Long storeId = 1L;
        Instant timestamp = Instant.now();
        
        Courier courier = createCourier(courierId);
        Store store = createStore(storeId);
        StoreEntryEvent event = createStoreEntryEvent(courierId, storeId, store.getName(), timestamp);

        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(storeEntryRepository.findLatestEntryForCourierAndStore(
                courierId, storeId, timestamp.minus(Duration.ofSeconds(STORE_ENTRY_COOLDOWN))))
                .thenReturn(Optional.empty());
        when(storeEntryRepository.save(any(StoreEntry.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        service.processStoreEntry(event);

        // Assert
        verify(storeEntryRepository).save(storeEntryCaptor.capture());
        StoreEntry savedEntry = storeEntryCaptor.getValue();
        assertEquals(courier, savedEntry.getCourier());
        assertEquals(store, savedEntry.getStore());
        assertEquals(event.getTimestamp(), savedEntry.getEntryTime());
        assertEquals(event.getLatitude(), savedEntry.getEntryLatitude());
        assertEquals(event.getLongitude(), savedEntry.getEntryLongitude());
    }

    @Test
    void processStoreEntry_WithinCooldownPeriod_ShouldNotSaveEntry() {
        // Arrange
        Long courierId = 1L;
        Long storeId = 1L;
        Instant timestamp = Instant.now();
        
        Courier courier = createCourier(courierId);
        Store store = createStore(storeId);
        StoreEntryEvent event = createStoreEntryEvent(courierId, storeId, store.getName(), timestamp);
        StoreEntry recentEntry = createStoreEntry(courier, store, timestamp.minus(Duration.ofSeconds(30)));

        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(storeEntryRepository.findLatestEntryForCourierAndStore(
                courierId, storeId, timestamp.minus(Duration.ofSeconds(STORE_ENTRY_COOLDOWN))))
                .thenReturn(Optional.of(recentEntry));

        // Act
        service.processStoreEntry(event);

        // Assert
        verify(storeEntryRepository, never()).save(any(StoreEntry.class));
    }

    @Test
    void processStoreEntry_CourierNotFound_ShouldThrowException() {
        // Arrange
        Long courierId = 1L;
        Long storeId = 1L;
        StoreEntryEvent event = createStoreEntryEvent(courierId, storeId, "Test Store", Instant.now());

        when(courierRepository.findById(courierId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            service.processStoreEntry(event)
        );
        assertTrue(exception.getMessage().contains("Courier not found"));
        verify(storeEntryRepository, never()).save(any(StoreEntry.class));
    }

    @Test
    void processStoreEntry_StoreNotFound_ShouldThrowException() {
        // Arrange
        Long courierId = 1L;
        Long storeId = 1L;
        Courier courier = createCourier(courierId);
        StoreEntryEvent event = createStoreEntryEvent(courierId, storeId, "Test Store", Instant.now());

        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            service.processStoreEntry(event)
        );
        assertTrue(exception.getMessage().contains("Store not found"));
        verify(storeEntryRepository, never()).save(any(StoreEntry.class));
    }

    private Courier createCourier(Long id) {
        return Courier.builder()
                .id(id)
                .totalTravelDistance(0.0)
                .lastLatitude(40.9923307)
                .lastLongitude(29.1244229)
                .isActive(true)
                .build();
    }

    private Store createStore(Long id) {
        return Store.builder()
                .id(id)
                .name("Test Store")
                .latitude(40.9923307)
                .longitude(29.1244229)
                .build();
    }

    private StoreEntryEvent createStoreEntryEvent(Long courierId, Long storeId, String storeName, Instant timestamp) {
        return StoreEntryEvent.builder()
                .courierId(courierId)
                .storeId(storeId)
                .storeName(storeName)
                .latitude(40.9923307)
                .longitude(29.1244229)
                .timestamp(timestamp)
                .build();
    }

    private StoreEntry createStoreEntry(Courier courier, Store store, Instant timestamp) {
        return StoreEntry.builder()
                .courier(courier)
                .store(store)
                .entryTime(timestamp)
                .entryLatitude(courier.getLastLatitude())
                .entryLongitude(courier.getLastLongitude())
                .build();
    }
} 