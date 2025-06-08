package com.example.delivery.eta.domain;

import com.example.delivery.order.domain.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
/* ETA 히스토리 도메인 (추후 피드백을 위한 도메인) */
public class EtaHistory {

    // ETA 히스토리 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    // 기존 ETA
    private LocalDateTime previousEta;

    // 새로운 ETA
    private LocalDateTime newEta;

    // ETA 변경 원인
    private String reason;

    // ETA 변경 시각
    private LocalDateTime adjustedAt;
}
