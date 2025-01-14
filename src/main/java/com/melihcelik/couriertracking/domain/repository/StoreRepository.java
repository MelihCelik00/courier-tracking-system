package com.melihcelik.couriertracking.domain.repository;

import com.melihcelik.couriertracking.domain.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
} 