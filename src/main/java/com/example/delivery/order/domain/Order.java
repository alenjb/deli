package com.example.delivery.order.domain;

import com.example.delivery.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
/* 주문 도메인 */
@Table(name = "orders")
public class Order {

    // 주문 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 ID
    private Long userId;

    // 매장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    // 매장과 목적지까지의 거리
    private double distanceKm;

    // 주문 시각
    private LocalDateTime createdAt;

    // 계산된 ETA
    private LocalDateTime eta;

    // 실제 도착 시간
    private LocalDateTime deliveredAt;

    /**
     * 배달 완료 시간을 설정하는 메서드
     * @param deliveredAt 배달 완료 시각
     */
    public void completeDelivery(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    /**
     * ETA보다 지연되었는지 판단하는 메서드
     * 기준은 5분
     * @return 지연 여부
     */
    public boolean isDelayed() {
        if (this.deliveredAt == null || this.eta == null) return false;
        return this.deliveredAt.isAfter(this.eta.plusMinutes(5));
    }

}
