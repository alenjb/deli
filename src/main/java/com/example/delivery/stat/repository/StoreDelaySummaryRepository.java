package com.example.delivery.stat.repository;

import com.example.delivery.stat.domain.StoreDelaySummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreDelaySummaryRepository extends JpaRepository<StoreDelaySummary, Long> {
}
