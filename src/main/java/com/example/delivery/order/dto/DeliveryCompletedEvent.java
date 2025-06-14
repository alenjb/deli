package com.example.delivery.order.dto;

import java.time.LocalDateTime;

/**
 * Kafka로부터 수신하는 배달 완료 이벤트
 */
public record DeliveryCompletedEvent(
        Long orderId,
        LocalDateTime deliveredAt
) {}
