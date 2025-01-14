package com.melihcelik.couriertracking.infrastructure.rest;

import com.melihcelik.couriertracking.application.query.CourierQueryService;
import com.melihcelik.couriertracking.domain.event.CourierLocationEvent;
import com.melihcelik.couriertracking.domain.model.Courier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/couriers")
@RequiredArgsConstructor
@Tag(name = "Courier API", description = "API endpoints for courier tracking")
public class CourierController {
    private final CourierQueryService queryService;
    private final KafkaTemplate<String, CourierLocationEvent> kafkaTemplate;

    @PostMapping("/{courierId}/locations")
    @Operation(summary = "Report courier location", description = "Report a new location for a courier")
    public ResponseEntity<Void> reportLocation(
            @PathVariable Long courierId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        
        CourierLocationEvent event = CourierLocationEvent.builder()
                .courierId(courierId)
                .latitude(latitude)
                .longitude(longitude)
                .build();

        kafkaTemplate.send("courier.location", event);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{courierId}")
    @Operation(summary = "Get courier details", description = "Get details of a specific courier")
    public ResponseEntity<Courier> getCourier(@PathVariable Long courierId) {
        return queryService.getCourierById(courierId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{courierId}/total-travel-distance")
    @Operation(summary = "Get total travel distance", description = "Get the total distance traveled by a courier")
    public ResponseEntity<Double> getTotalTravelDistance(@PathVariable Long courierId) {
        double distance = queryService.getTotalTravelDistance(courierId);
        return ResponseEntity.ok(distance);
    }
} 