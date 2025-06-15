package com.example.delivery.stat.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 매장의 배달 지연 통계를 응답하는 DTO
 */
@Getter
@Builder
public class StoreDelaySummaryResponse {

    // 매장 ID
    private Long storeId;
    // 매장 이름
    private String storeName;
    // 총 주문 수
    private int totalOrders;
    // 지연된 주문 수
    private int delayedOrders;
    // 총 지연 시간 (분)
    private long totalDelayMinutes;
    // 지연율 (지연 주문 수 / 총 주문 수) * 100
    private double delayRate;
}
