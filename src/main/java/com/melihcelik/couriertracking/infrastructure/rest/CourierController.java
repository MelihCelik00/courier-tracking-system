package com.melihcelik.couriertracking.infrastructure.rest;

import com.melihcelik.couriertracking.application.query.CourierQueryService;
import com.melihcelik.couriertracking.domain.event.CourierLocationEvent;
import com.melihcelik.couriertracking.domain.exception.CourierNotFoundException;
import com.melihcelik.couriertracking.domain.exception.InvalidLocationException;
import com.melihcelik.couriertracking.domain.model.Courier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiEndpoints.COURIERS)
@RequiredArgsConstructor
@Tag(name = "Courier API", description = "API endpoints for courier tracking")
public class CourierController {
    private final CourierQueryService queryService;
    private final KafkaTemplate<String, CourierLocationEvent> kafkaTemplate;

    @PostMapping(ApiEndpoints.REPORT_LOCATION)
    @Operation(summary = "Report courier location", description = "Report a new location for a courier")
    public ResponseEntity<Void> reportLocation(
            @PathVariable Long courierId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        
        validateLocation(latitude, longitude);
        
        CourierLocationEvent event = CourierLocationEvent.builder()
                .courierId(courierId)
                .latitude(latitude)
                .longitude(longitude)
                .build();

        kafkaTemplate.send("courier.location", event);
        return ResponseEntity.accepted().build();
    }

    @GetMapping(ApiEndpoints.GET_COURIER)
    @Operation(summary = "Get courier details", description = "Get details of a specific courier")
    public ResponseEntity<Courier> getCourier(@PathVariable Long courierId) {
        return queryService.getCourierById(courierId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CourierNotFoundException(courierId));
    }

    @GetMapping(ApiEndpoints.GET_TOTAL_TRAVEL_DISTANCE)
    @Operation(summary = "Get total travel distance", description = "Get the total distance traveled by a courier")
    public ResponseEntity<Double> getTotalTravelDistance(@PathVariable Long courierId) {
        if (!queryService.getCourierById(courierId).isPresent()) {
            throw new CourierNotFoundException(courierId);
        }
        double distance = queryService.getTotalTravelDistance(courierId);
        return ResponseEntity.ok(distance);
    }

    private void validateLocation(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new InvalidLocationException("Latitude and longitude must not be null");
        }
        if (latitude < -90 || latitude > 90) {
            throw new InvalidLocationException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new InvalidLocationException("Longitude must be between -180 and 180");
        }
    }
} 