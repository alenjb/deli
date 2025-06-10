package com.example.delivery.review.domain;

import com.example.delivery.order.domain.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // 사용자가 남긴 리뷰 내용
    private String content;

    // 추출한 소요 시간
        // ex) "30분 만에 왔어요" → 30
    private Integer extractedDeliveryMinutes;

    private LocalDateTime createdAt;
}
