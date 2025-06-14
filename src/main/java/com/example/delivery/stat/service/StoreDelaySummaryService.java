package com.example.delivery.stat.service;

import com.example.delivery.order.domain.Order;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.stat.domain.StoreDelaySummary;
import com.example.delivery.stat.repository.StoreDelaySummaryRepository;
import com.example.delivery.store.domain.Store;
import com.example.delivery.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreDelaySummaryService {

    private final StoreDelaySummaryRepository summaryRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    /**
     * 특정 매장의 지연 통계를 갱신하는 메서드
     * @param storeId 매장 ID
     */
    public void updateDelayStats(Long storeId) {
        // 1. 매장 정보 조회
        Optional<Store> optionalStore = storeRepository.findById(storeId);
        if (optionalStore.isEmpty()) {
            throw new RuntimeException("매장을 찾을 수 없습니다.");
        }
        Store store = optionalStore.get();

        // 2. 기존 통계 정보 조회 (없으면 새로 생성)
        Optional<StoreDelaySummary> optionalSummary = summaryRepository.findById(storeId);
        StoreDelaySummary summary = optionalSummary.orElse(
                StoreDelaySummary.builder()
                        .storeId(storeId)
                        .storeName(store.getName()) // 정확한 매장 이름 저장
                        .totalOrders(0)
                        .delayedOrders(0)
                        .totalDelayMinutes(0)
                        .lastAnalyzedAt(LocalDateTime.MIN) // 초기값
                        .build()
        );

        // 3. 마지막 통계 이후 배달된 주문 조회
        List<Order> recentOrders = orderRepository
                .findByStoreIdAndDeliveredAtAfter(storeId, summary.getLastAnalyzedAt());

        // 4. 새 주문이 없으면 종료
        if (recentOrders.isEmpty()) return;

        // 5. 지연 통계 누적값 계산
        int newOrders = recentOrders.size();
        int newDelayed = 0;
        long newDelayMinutes = 0;
        LocalDateTime lastTime = summary.getLastAnalyzedAt();

        for (Order order : recentOrders) {
            if (order.getDeliveredAt() != null) { // 배달이 완료 된 것만
                // lastTime을 가장 최근 배달 시간으로 업데이트
                if (order.getDeliveredAt().isAfter(lastTime)) {
                    lastTime = order.getDeliveredAt();
                }

                // 지연 주문 처리
                if (order.isDelayed()) {
                    newDelayed++; // 지연 주문 수 증가
                    long delay = java.time.Duration.between(order.getEta(), order.getDeliveredAt()).toMinutes();
                    newDelayMinutes += delay;
                }
            }
        }

        // 6. 통계 정보 업데이트 및 DB 저장
        summary.updateStats(newOrders, newDelayed, newDelayMinutes, lastTime);
        summaryRepository.save(summary);
    }
}
