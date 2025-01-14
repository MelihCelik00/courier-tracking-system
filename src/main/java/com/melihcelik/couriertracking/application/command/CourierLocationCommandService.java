package com.melihcelik.couriertracking.application.command;

import com.melihcelik.couriertracking.domain.event.CourierLocationEvent;
import com.melihcelik.couriertracking.domain.event.StoreEntryEvent;
import com.melihcelik.couriertracking.domain.model.Courier;
import com.melihcelik.couriertracking.domain.model.Store;
import com.melihcelik.couriertracking.domain.repository.CourierRepository;
import com.melihcelik.couriertracking.domain.repository.StoreRepository;
import com.melihcelik.couriertracking.domain.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        Courier courier = courierRepository.findById(event.getCourierId())
                .orElseGet(() -> createNewCourier(event));

        updateCourierLocation(courier, event);
        checkStoreProximity(courier);
        
        // Publish the location event for distance calculation
        locationKafkaTemplate.send("courier.location", event);
    }

    private Courier createNewCourier(CourierLocationEvent event) {
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
        }
        
        courier.setLastLatitude(event.getLatitude());
        courier.setLastLongitude(event.getLongitude());
        courierRepository.save(courier);
    }

    private void checkStoreProximity(Courier courier) {
        List<Store> stores = storeRepository.findAll();
        
        for (Store store : stores) {
            if (GeoUtils.isWithinRadius(
                    courier.getLastLatitude(), courier.getLastLongitude(),
                    store.getLatitude(), store.getLongitude(),
                    storeProximityRadius
            )) {
                StoreEntryEvent entryEvent = StoreEntryEvent.builder()
                        .courierId(courier.getId())
                        .storeId(store.getId())
                        .storeName(store.getName())
                        .latitude(courier.getLastLatitude())
                        .longitude(courier.getLastLongitude())
                        .build();
                
                storeEntryKafkaTemplate.send("store.entry", entryEvent);
            }
        }
    }
} 