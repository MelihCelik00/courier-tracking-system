package com.melihcelik.couriertracking.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    @NotNull
    private Courier courier;

    @ManyToOne
    @JoinColumn(name = "store_id")
    @NotNull
    private Store store;

    @NotNull
    private Instant entryTime;

    @NotNull
    private Double entryLatitude;

    @NotNull
    private Double entryLongitude;
}