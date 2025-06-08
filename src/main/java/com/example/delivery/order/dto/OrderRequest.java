package com.example.delivery.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/* 주문 요청 DTO */
public class OrderRequest {
    private Long userId;

    private Long storeId;

    // 매장과 목적지까지의 거리
    private double distanceKm;

    // 지도API에서 받은 예상 배달 소요 시간(분)
    private int estimatedDeliveryTimeMinutes;
}
