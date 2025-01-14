package com.melihcelik.couriertracking.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreEntryEvent {
    private Long courierId;
    private Long storeId;
    private String storeName;
    private Double latitude;
    private Double longitude;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
} 