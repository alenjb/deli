package com.example.delivery.stat.repository;

import com.example.delivery.stat.domain.StoreDelaySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreDelaySummaryRepository extends JpaRepository<StoreDelaySummary, Long> {

    // 매장 ID로 통계 조회
    Optional<StoreDelaySummary> findByStoreId(Long storeId);
}
