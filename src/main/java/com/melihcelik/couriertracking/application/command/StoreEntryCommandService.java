package com.melihcelik.couriertracking.application.command;

import com.melihcelik.couriertracking.domain.event.StoreEntryEvent;
import com.melihcelik.couriertracking.domain.model.Courier;
import com.melihcelik.couriertracking.domain.model.Store;
import com.melihcelik.couriertracking.domain.model.StoreEntry;
import com.melihcelik.couriertracking.domain.repository.CourierRepository;
import com.melihcelik.couriertracking.domain.repository.StoreEntryRepository;
import com.melihcelik.couriertracking.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreEntryCommandService {
    private final StoreEntryRepository storeEntryRepository;
    private final CourierRepository courierRepository;
    private final StoreRepository storeRepository;

    @Value("${courier-tracking.store-entry-cooldown}")
    private int storeEntryCooldown;

    @Transactional
    public void processStoreEntry(StoreEntryEvent event) {
        try {
            log.debug("Processing store entry event: {}", event);
            
            Courier courier = courierRepository.findById(event.getCourierId())
                    .orElseThrow(() -> {
                        log.error("Courier not found for store entry event - courierId: {}", event.getCourierId());
                        return new IllegalStateException("Courier not found with ID: " + event.getCourierId());
                    });
            
            Store store = storeRepository.findById(event.getStoreId())
                    .orElseThrow(() -> {
                        log.error("Store not found for store entry event - storeId: {}", event.getStoreId());
                        return new IllegalStateException("Store not found with ID: " + event.getStoreId());
                    });

            Instant cooldownTime = event.getTimestamp().minus(Duration.ofSeconds(storeEntryCooldown));
            boolean recentEntry = storeEntryRepository
                    .findLatestEntryForCourierAndStore(courier.getId(), store.getId(), cooldownTime)
                    .isPresent();

            if (recentEntry) {
                log.debug("Skipping store entry due to cooldown - courier: {}, store: {}, cooldownTime: {}", 
                        courier.getId(), store.getId(), cooldownTime);
                return;
            }

            log.info("Creating new store entry - courier: {}, store: {}, timestamp: {}",
                    courier.getId(), store.getId(), event.getTimestamp());

            StoreEntry entry = StoreEntry.builder()
                    .courier(courier)
                    .store(store)
                    .entryTime(event.getTimestamp())
                    .entryLatitude(event.getLatitude())
                    .entryLongitude(event.getLongitude())
                    .build();

            storeEntryRepository.save(entry);
            log.debug("Successfully saved store entry - id: {}, courier: {}, store: {}", 
                    entry.getId(), courier.getId(), store.getId());
            
        } catch (Exception e) {
            log.error("Error processing store entry event: {}", event, e);
            throw e; // Re-throw to trigger Kafka retry mechanism
        }
    }
}