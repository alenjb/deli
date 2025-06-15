package com.example.delivery.order.controller;

import com.example.delivery.kafka.DeliveryEventProducer;
import com.example.delivery.order.dto.OrderRequest;
import com.example.delivery.order.dto.OrderResponse;
import com.example.delivery.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final DeliveryEventProducer deliveryEventProducer;

    /**
     * 주문을 생성하는 API
     * @param request 주문 요청 DTO
     * @return 주문 ID 및 ETA가 포함된 응답 DTO
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        OrderResponse response = orderService.makeOrder(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 배달을 완료하는 API
     * @param orderId 주문 ID
     * @param deliveredAt 배달 완료 시각
     * @return "배달 완료" 라는 문자열 
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<String> completeOrder(@PathVariable Long orderId, @RequestParam String deliveredAt // 예: "2025-06-15T13:30"
    ) {
        LocalDateTime deliveredTime = LocalDateTime.parse(deliveredAt);
        orderService.completeDelivery(orderId, deliveredTime);
        return ResponseEntity.ok("배달 완료");
    }

    /**
     * 특정 주문이 배달 완료되었음을 Kafka를 통해 전송하는 API
     *
     * @param orderId 배달 완료된 주문 ID
     * @param deliveredAt 실제 배달 완료 시각
     * @return 전송 결과 메시지
     */
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<String> confirmDelivery(@PathVariable Long orderId,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deliveredAt) {

        // Kafka 토픽에 배달 완료 메시지를 전송
        deliveryEventProducer.send("delivery-status", orderId, deliveredAt);
        return ResponseEntity.ok("배달 완료 메시지를 Kafka로 전송");
    }
}
