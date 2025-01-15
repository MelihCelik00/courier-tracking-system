package com.melihcelik.couriertracking.application.query;

import com.melihcelik.couriertracking.domain.exception.CourierNotFoundException;
import com.melihcelik.couriertracking.domain.model.Courier;
import com.melihcelik.couriertracking.domain.repository.CourierRepository;
import com.melihcelik.couriertracking.domain.repository.StoreEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourierQueryService {
    private final CourierRepository courierRepository;
    private final StoreEntryRepository storeEntryRepository;

    public Optional<Courier> getCourierById(Long courierId) {
        return courierRepository.findById(courierId);
    }

    public double getTotalTravelDistance(Long courierId) {
        return courierRepository.findById(courierId)
                .map(Courier::getTotalTravelDistance)
                .orElseThrow(() -> new CourierNotFoundException(courierId));
    }
} 