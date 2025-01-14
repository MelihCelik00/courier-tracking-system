package com.melihcelik.couriertracking.domain.repository;

import com.melihcelik.couriertracking.domain.model.StoreEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface StoreEntryRepository extends JpaRepository<StoreEntry, Long> {
    
    @Query("SELECT se FROM StoreEntry se " +
           "WHERE se.courier.id = :courierId " +
           "AND se.store.id = :storeId " +
           "AND se.entryTime >= :since " +
           "ORDER BY se.entryTime DESC")
    Optional<StoreEntry> findLatestEntryForCourierAndStore(
            @Param("courierId") Long courierId,
            @Param("storeId") Long storeId,
            @Param("since") LocalDateTime since);
} 