package com.example.delivery.order.dto;

import com.example.delivery.order.domain.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
/* 주문 응답 DTO */
public class OrderResponse {
    private Long orderId;
    private DeliveryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private Long storeId;
}
