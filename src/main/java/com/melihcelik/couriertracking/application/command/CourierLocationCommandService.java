package com.melihcelik.couriertracking.application.command;

import com.melihcelik.couriertracking.domain.event.CourierLocationEvent;
import com.melihcelik.couriertracking.domain.event.StoreEntryEvent;
import com.melihcelik.couriertracking.domain.model.Courier;
import com.melihcelik.couriertracking.domain.model.Store;
import com.melihcelik.couriertracking.domain.repository.CourierRepository;
import com.melihcelik.couriertracking.domain.repository.StoreRepository;
import com.melihcelik.couriertracking.domain.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourierLocationCommandService {
    private final CourierRepository courierRepository;
    private final StoreRepository storeRepository;
    private final KafkaTemplate<String, StoreEntryEvent> storeEntryKafkaTemplate;
    private final KafkaTemplate<String, CourierLocationEvent> locationKafkaTemplate;

    @Value("${courier-tracking.store-proximity-radius}")
    private double storeProximityRadius;

    @Transactional
    public void processCourierLocation(CourierLocationEvent event) {
        log.debug("Processing courier location event: {}", event);
        
        Courier courier = courierRepository.findById(event.getCourierId())
                .orElseGet(() -> createNewCourier(event));
        
        updateCourierLocation(courier, event);
        courier = courierRepository.save(courier);

        final Long courierId = courier.getId();
        final CourierLocationEvent finalEvent = event;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                courierRepository.findById(courierId).ifPresent(committedCourier ->
                    checkStoreProximity(committedCourier, finalEvent));
            }
        });
    }

    private Courier createNewCourier(CourierLocationEvent event) {
        log.info("Creating new courier with ID: {}", event.getCourierId());
        return Courier.builder()
                .id(event.getCourierId())
                .totalTravelDistance(0.0)
                .lastLatitude(event.getLatitude())
                .lastLongitude(event.getLongitude())
                .isActive(true)
                .build();
    }

    private void updateCourierLocation(Courier courier, CourierLocationEvent event) {
        if (courier.getLastLatitude() != null && courier.getLastLongitude() != null) {
            double distance = GeoUtils.calculateDistance(
                    courier.getLastLatitude(), courier.getLastLongitude(),
                    event.getLatitude(), event.getLongitude()
            );
            courier.setTotalTravelDistance(courier.getTotalTravelDistance() + distance);
            log.debug("Updated courier total distance - courier: {}, distance: {}", courier.getId(), distance);
        }
        
        courier.setLastLatitude(event.getLatitude());
        courier.setLastLongitude(event.getLongitude());
    }

    private void checkStoreProximity(Courier courier, CourierLocationEvent event) {
        List<Store> stores = storeRepository.findAll();
        
        for (Store store : stores) {
            boolean isWithinRadius = GeoUtils.isWithinRadius(
                    courier.getLastLatitude(), courier.getLastLongitude(),
                    store.getLatitude(), store.getLongitude(),
                    storeProximityRadius
            );

            if (isWithinRadius) {
                log.debug("Courier within store radius - courier: {}, store: {}", courier.getId(), store.getId());
                
                StoreEntryEvent entryEvent = StoreEntryEvent.builder()
                        .courierId(courier.getId())
                        .storeId(store.getId())
                        .storeName(store.getName())
                        .latitude(courier.getLastLatitude())
                        .longitude(courier.getLastLongitude())
                        .timestamp(event.getTimestamp())
                        .build();
                
                storeEntryKafkaTemplate.send("store.entry", entryEvent)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.debug("Successfully published store entry event - courier: {}, store: {}", 
                                courier.getId(), store.getId());
                        } else {
                            log.error("Failed to publish store entry event - courier: {}, store: {}, error: {}", 
                                courier.getId(), store.getId(), ex.toString());
                        }
                    });
            }
        }
    }
} 