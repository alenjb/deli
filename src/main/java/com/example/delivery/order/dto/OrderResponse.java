package com.example.delivery.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
/* 주문 응답 DTO */
public class OrderResponse {
    private Long orderId;

    private LocalDateTime estimatedArrivalTime;
}
