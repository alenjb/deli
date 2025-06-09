package com.example.delivery.order.controller;

import com.example.delivery.order.dto.OrderRequest;
import com.example.delivery.order.dto.OrderResponse;
import com.example.delivery.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

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
}
