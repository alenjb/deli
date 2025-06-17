package com.example.delivery.eta.scheduler;

import com.example.delivery.order.domain.Order;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.stat.service.StoreDelaySummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EtaScheduler {

    private final OrderRepository orderRepository;
    private final StoreDelaySummaryService summaryService;

    /**
     * 매일 자정에 하루 동안 배달 완료된 주문을 기반으로 각 매장의 지연 통계를 누적 반영하는 스케줄러 메서드
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void updateDelayStatsForAllStores() {
        log.info("------[ETA 갱신 시작]------");

        LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay(); // 어제 00:00
        LocalDateTime end = LocalDate.now().atStartOfDay();                // 오늘 00:00

        List<Order> completedOrders = orderRepository.findByDeliveredAtBetween(start, end);

        // 매장별 주문 그룹핑을 위한 Map
        Map<Long, List<Order>> ordersByStore = new HashMap<>();

        // 전체 배달 완료 주문 리스트를 매장별로 그룹핑하여 Map에 저장
        for (Order order : completedOrders) {
            Long storeId = order.getStore().getId();

            // 매장 ID가 없으면 새 리스트를 생성
            if (!ordersByStore.containsKey(storeId)) {
                ordersByStore.put(storeId, new ArrayList<>());
            }

            // 해당 매장 리스트에 주문 추가
            ordersByStore.get(storeId).add(order);
        }

        // 매장별 주문 통계를 계산하고 저장
        for (Map.Entry<Long, List<Order>> entry : ordersByStore.entrySet()) {
            Long storeId = entry.getKey();
            List<Order> orders = entry.getValue();

            int total = orders.size();          // 총 주문 수
            int delayed = 0;                    // 지연된 주문 수
            long totalDelayMinutes = 0L;        // 지연된 시간의 총합
            LocalDateTime latestDeliveredAt = null;  // 가장 마지막 배달 시각

            // 주문 목록 순회하며 지연 여부 판단 및 통계 누적
            for (Order order : orders) {
                if (order.isDelayed()) {
                    delayed++;

                    // 지연 시간(ETA와 실제 배달 시간의 차이)를 계산
                    Duration delay = Duration.between(order.getEta(), order.getDeliveredAt());
                    totalDelayMinutes += delay.toMinutes();
                }

                // 가장 마지막 배달 시간 찾기
                if (latestDeliveredAt == null || order.getDeliveredAt().isAfter(latestDeliveredAt)) {
                    latestDeliveredAt = order.getDeliveredAt();
                }
            }

            // 배달 시각이 없는 경우
                // 1. 아직 배달이 완료되지 않은 경우(배달 중)
                // 2. 배달이 없는 경우
            if (latestDeliveredAt == null) {
                log.info("분석 제외 - storeId: {} (해당 매장에 배달 완료된 주문 없음)", storeId);
                continue; // 다음 매장으로 넘어감
            }

            try {
                // 통계 업데이트
                summaryService.update(storeId, total, delayed, totalDelayMinutes, latestDeliveredAt);

                // 로그 출력용 지연률 계산
                double delayRate = (double) delayed * 100.0 / total;
                log.info("통계 업데이트 완료 - storeId: {}, 총 주문: {}, 지연: {}, 지연률: {}%",
                        storeId, total, delayed, delayRate);

            } catch (RuntimeException e) { // 통계 업데이트 오류
                log.warn("통계 업데이트 실패 - storeId: {}, 원인: {}", storeId, e.getMessage());
            }
        }

        // 전체 매장 수 출력
        log.info("------[ETA 통계 갱신 완료] 총 매장 수: {} ------", ordersByStore.size());

    }
}