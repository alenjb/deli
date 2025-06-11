package com.example.delivery.stat.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreDelaySummary {

    @Id
    private Long storeId;

    private String storeName;

    private int totalOrders;

    // 총 지연된 주문
    private int delayedOrders;

    private long totalDelayMinutes;

    // 마지막으로 지연율을 계산한 시각
    private LocalDateTime lastAnalyzedAt;

    /**
     * 지연율 계산에 필요한 정보를 업데이트하는 메서드
     * @param newOrders 새 주문들의 개수
     * @param newDelayed 새 주문들 중 지연된 주문들의 개수
     * @param newDelayMinutes 지연된 주문들의 지연 시간합
     * @param analyzedAt 제일 마지막 주문 시각
     */
    public void updateStats(int newOrders, int newDelayed, long newDelayMinutes, LocalDateTime analyzedAt) {
        this.totalOrders += newOrders;
        this.delayedOrders += newDelayed;
        this.totalDelayMinutes += newDelayMinutes;
        this.lastAnalyzedAt = analyzedAt;
    }

    /**
     * 지연을을 계산하는 메서드
     * @return 지연율
     */
    public double getDelayRate() {
        return totalOrders == 0 ? 0.0 : (double) delayedOrders / totalOrders;
    }

    /**
     * 평균 지연시간을 계산하는 메서드
     * @return 평균 지연시간
     */
    public double getAvgDelayMinutes() {
        return delayedOrders == 0 ? 0.0 : (double) totalDelayMinutes / delayedOrders;
    }
}
