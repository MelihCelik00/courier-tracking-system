package com.melihcelik.couriertracking.domain.repository;

import com.melihcelik.couriertracking.domain.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
} 