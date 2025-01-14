package com.melihcelik.couriertracking.application.command;

import com.melihcelik.couriertracking.domain.event.StoreEntryEvent;
import com.melihcelik.couriertracking.domain.model.Courier;
import com.melihcelik.couriertracking.domain.model.Store;
import com.melihcelik.couriertracking.domain.model.StoreEntry;
import com.melihcelik.couriertracking.domain.repository.CourierRepository;
import com.melihcelik.couriertracking.domain.repository.StoreEntryRepository;
import com.melihcelik.couriertracking.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        Courier courier = courierRepository.findById(event.getCourierId())
                .orElseThrow(() -> new IllegalStateException("Courier not found"));
        
        Store store = storeRepository.findById(event.getStoreId())
                .orElseThrow(() -> new IllegalStateException("Store not found"));

        // Check if courier has entered this store recently
        LocalDateTime cooldownTime = event.getTimestamp().minusSeconds(storeEntryCooldown);
        boolean recentEntry = storeEntryRepository
                .findLatestEntryForCourierAndStore(courier.getId(), store.getId(), cooldownTime)
                .isPresent();

        if (!recentEntry) {
            StoreEntry entry = StoreEntry.builder()
                    .courier(courier)
                    .store(store)
                    .entryTime(event.getTimestamp())
                    .entryLatitude(event.getLatitude())
                    .entryLongitude(event.getLongitude())
                    .build();

            storeEntryRepository.save(entry);
        }
    }
} 