package com.melihcelik.couriertracking.application.command;

import com.melihcelik.couriertracking.domain.event.CourierLocationEvent;
import com.melihcelik.couriertracking.domain.event.StoreEntryEvent;
import com.melihcelik.couriertracking.domain.model.Courier;
import com.melihcelik.couriertracking.domain.model.Store;
import com.melihcelik.couriertracking.domain.repository.CourierRepository;
import com.melihcelik.couriertracking.domain.repository.StoreRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierLocationCommandServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private KafkaTemplate<String, StoreEntryEvent> storeEntryKafkaTemplate;

    @Mock
    private KafkaTemplate<String, CourierLocationEvent> locationKafkaTemplate;

    @Captor
    private ArgumentCaptor<Courier> courierCaptor;

    @Captor
    private ArgumentCaptor<StoreEntryEvent> storeEntryEventCaptor;

    private CourierLocationCommandService service;
    private MockedStatic<TransactionSynchronizationManager> mockedStatic;

    private static final double STORE_PROXIMITY_RADIUS = 100.0; // meters

    @BeforeEach
    void setUp() {
        service = new CourierLocationCommandService(
                courierRepository,
                storeRepository,
                storeEntryKafkaTemplate,
                locationKafkaTemplate
        );
        ReflectionTestUtils.setField(service, "storeProximityRadius", STORE_PROXIMITY_RADIUS);
        
        // Mock TransactionSynchronizationManager
        mockedStatic = mockStatic(TransactionSynchronizationManager.class);
        when(TransactionSynchronizationManager.isSynchronizationActive()).thenReturn(true);
        doAnswer(invocation -> {
            TransactionSynchronization synchronization = invocation.getArgument(0);
            synchronization.afterCommit();
            return null;
        }).when(TransactionSynchronizationManager.class);
        TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class));
    }

    @AfterEach
    void tearDown() {
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }

    @Test
    void processCourierLocation_NewCourier_ShouldCreateAndSave() {
        // Arrange
        Long courierId = 1L;
        CourierLocationEvent event = createLocationEvent(courierId, 40.9923307, 29.1244229);
        when(courierRepository.findById(courierId)).thenReturn(Optional.empty());
        when(courierRepository.save(any(Courier.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        service.processCourierLocation(event);

        // Assert
        verify(courierRepository).save(courierCaptor.capture());
        Courier savedCourier = courierCaptor.getValue();
        assertEquals(courierId, savedCourier.getId());
        assertEquals(0.0, savedCourier.getTotalTravelDistance());
        assertEquals(event.getLatitude(), savedCourier.getLastLatitude());
        assertEquals(event.getLongitude(), savedCourier.getLastLongitude());
        assertTrue(savedCourier.getIsActive());
    }

    @Test
    void processCourierLocation_ExistingCourier_ShouldUpdateLocation() {
        // Arrange
        Long courierId = 1L;
        Courier existingCourier = Courier.builder()
                .id(courierId)
                .lastLatitude(40.9923307)
                .lastLongitude(29.1244229)
                .totalTravelDistance(0.0)
                .isActive(true)
                .build();

        CourierLocationEvent event = createLocationEvent(courierId, 40.986106, 29.1161293);
        when(courierRepository.findById(courierId)).thenReturn(Optional.of(existingCourier));
        when(courierRepository.save(any(Courier.class))).thenAnswer(i -> i.getArgument(0));
        when(storeRepository.findAll()).thenReturn(List.of());

        // Act
        service.processCourierLocation(event);

        // Assert
        verify(courierRepository).save(courierCaptor.capture());
        Courier updatedCourier = courierCaptor.getValue();
        assertEquals(courierId, updatedCourier.getId());
        assertTrue(updatedCourier.getTotalTravelDistance() > 0.0);
        assertEquals(event.getLatitude(), updatedCourier.getLastLatitude());
        assertEquals(event.getLongitude(), updatedCourier.getLastLongitude());
    }

    @Test
    void processCourierLocation_CourierNearStore_ShouldPublishStoreEntryEvent() {
        // Arrange
        Long courierId = 1L;
        Long storeId = 1L;
        String storeName = "Test Store";
        
        Store store = Store.builder()
                .id(storeId)
                .name(storeName)
                .latitude(40.9923307)
                .longitude(29.1244229)
                .build();

        CourierLocationEvent event = createLocationEvent(courierId, 40.9923307, 29.1244229);
        Courier courier = Courier.builder()
                .id(courierId)
                .lastLatitude(event.getLatitude())
                .lastLongitude(event.getLongitude())
                .totalTravelDistance(0.0)
                .isActive(true)
                .build();

        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));
        when(courierRepository.save(any(Courier.class))).thenReturn(courier);
        when(storeRepository.findAll()).thenReturn(List.of(store));
        when(storeEntryKafkaTemplate.send(anyString(), any(StoreEntryEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        service.processCourierLocation(event);

        // Assert
        verify(storeEntryKafkaTemplate).send(eq("store.entry"), storeEntryEventCaptor.capture());
        StoreEntryEvent capturedEvent = storeEntryEventCaptor.getValue();
        assertEquals(courierId, capturedEvent.getCourierId());
        assertEquals(storeId, capturedEvent.getStoreId());
        assertEquals(storeName, capturedEvent.getStoreName());
        assertEquals(event.getLatitude(), capturedEvent.getLatitude());
        assertEquals(event.getLongitude(), capturedEvent.getLongitude());
    }

    private CourierLocationEvent createLocationEvent(Long courierId, double latitude, double longitude) {
        return CourierLocationEvent.builder()
                .courierId(courierId)
                .latitude(latitude)
                .longitude(longitude)
                .timestamp(Instant.now())
                .build();
    }
} 