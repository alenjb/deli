package com.example.delivery.stat.service;

import com.example.delivery.order.domain.Order;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.stat.domain.StoreDelaySummary;
import com.example.delivery.stat.dto.StoreDelaySummaryResponse;
import com.example.delivery.stat.repository.StoreDelaySummaryRepository;
import com.example.delivery.store.domain.Store;
import com.example.delivery.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreDelaySummaryService {

    private final StoreDelaySummaryRepository summaryRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    /**
     * 특정 매장의 지연 통계를 갱신하는 메서드
     *
     * 처리 로직:
     * 1. 매장 ID를 통해 매장 정보를 조회한다.
     *    - 매장이 존재하지 않으면 예외 발생.
     * 2. StoreDelaySummary(지연 통계)를 조회하거나 없으면 초기값으로 생성한다.
     * 3. 마지막 분석 시점 이후에 배달 완료된 주문들을 조회한다.
     * 4. 조회된 주문들 중에서:
     *    - 총 주문 수 계산
     *    - 지연된 주문 수 계산 (ETA보다 5분 이상 늦은 경우)
     *    - 총 지연 시간 계산 (ETA와 실제 도착 시간의 차이 누적)
     *    - 가장 마지막 배달 완료 시각을 기록
     * 5. 계산된 정보를 기반으로 StoreDelaySummary의 통계를 갱신하고 DB에 저장한다.
     *
     * @param storeId 매장 ID
     */
    public void updateDelayStats(Long storeId) {
        // 1. 매장 정보 조회
        Optional<Store> optionalStore = storeRepository.findById(storeId);
        if (optionalStore.isEmpty()) {
            throw new RuntimeException("매장을 찾을 수 없습니다.");
        }
        Store store = optionalStore.get();

        // 2. 기존 통계 정보 조회 (없으면 새로 생성)
        Optional<StoreDelaySummary> optionalSummary = summaryRepository.findById(storeId);
        StoreDelaySummary summary = optionalSummary.orElse(
                StoreDelaySummary.builder()
                        .storeId(storeId)
                        .storeName(store.getName()) // 정확한 매장 이름 저장
                        .totalOrders(0)
                        .delayedOrders(0)
                        .totalDelayMinutes(0)
                        .lastAnalyzedAt(LocalDateTime.MIN) // 초기값
                        .build()
        );

        // 3. 마지막 통계 이후 배달된 주문 조회
        List<Order> recentOrders = orderRepository
                .findByStoreIdAndDeliveredAtAfter(storeId, summary.getLastAnalyzedAt());

        // 4. 새 주문이 없으면 종료
        if (recentOrders.isEmpty()) return;

        // 5. 지연 통계 누적값 계산
        int newOrders = recentOrders.size();
        int newDelayed = 0;
        long newDelayMinutes = 0;
        LocalDateTime lastTime = summary.getLastAnalyzedAt();

        for (Order order : recentOrders) {
            if (order.getDeliveredAt() != null) { // 배달이 완료 된 것만
                // lastTime을 가장 최근 배달 시간으로 업데이트
                if (order.getDeliveredAt().isAfter(lastTime)) {
                    lastTime = order.getDeliveredAt();
                }

                // 지연 주문 처리
                if (order.isDelayed()) {
                    newDelayed++; // 지연 주문 수 증가
                    long delay = java.time.Duration.between(order.getEta(), order.getDeliveredAt()).toMinutes();
                    newDelayMinutes += delay;
                }
            }
        }

        // 6. 통계 정보 업데이트 및 DB 저장
        summary.updateStats(newOrders, newDelayed, newDelayMinutes, lastTime);
        summaryRepository.save(summary);
    }

    /**
     * 배달 완료된 주문을 기반으로 해당 매장의 지연 통계를 갱신하는 메서드
     * 처리 로직:
     * 1. 주문에서 매장 정보를 추출
     * 2. 해당 매장의 지연 통계(StoreDelaySummary)를 조회하거나 없으면 새로 생성
     * 3. 해당 주문이 지연되었는지 판단하고 통계 수치 계산
     *    - 총 주문 수 +1
     *    - 지연 주문 수 +1 (지연 시)
     *    - 총 지연 시간 추가
     *    - 마지막 분석 시각 갱신
     * 4. 갱신된 통계를 저장
     *
     * @param order 배달 완료된 주문
     */
    public void processCompletedOrder(Order order) {
        Store store = order.getStore();
        Long storeId = store.getId();

        // StoreDelaySummary를 가져오거나 없으면 초기 값으로 생성
        Optional<StoreDelaySummary> optionalSummary = summaryRepository.findById(storeId);
        StoreDelaySummary summary = optionalSummary.orElse(
                StoreDelaySummary.builder()
                        .storeId(storeId)
                        .storeName(store.getName()) // 매장 이름 저장
                        .totalOrders(0)
                        .delayedOrders(0)
                        .totalDelayMinutes(0)
                        .lastAnalyzedAt(LocalDateTime.MIN) // 초기값으로 설정
                        .build()
        );

        // 신규 통계 수치 계산
        int newOrders = 1;
        int newDelayed = order.isDelayed() ? 1 : 0;
        long delayMinutes = order.isDelayed()
                ? Duration.between(order.getEta(), order.getDeliveredAt()).toMinutes()
                : 0;
        LocalDateTime lastTime = order.getDeliveredAt();

        // 통계 정보 갱신
        summary.updateStats(newOrders, newDelayed, delayMinutes, lastTime);

        // DB 저장
        summaryRepository.save(summary);
    }

    /**
     * 지연률 기준으로 매장을 순위화하여 반환하는 메서드
     * 각 StoreDelaySummary 엔티티를 DTO로 변환하고, 지연률(delayRate)이 높은 순으로 정렬하여 리스트로 반환
     * @return 지연률 기준 정렬된 매장 통계 리스트
     */
    public List<StoreDelaySummaryResponse> getStoreRanking() {
        List<StoreDelaySummary> summaries = summaryRepository.findAll();
        List<StoreDelaySummaryResponse> responseList = new ArrayList<>();

        for (StoreDelaySummary summary : summaries) {
            StoreDelaySummaryResponse response = StoreDelaySummaryResponse.builder()
                    .storeId(summary.getStoreId())
                    .storeName(summary.getStoreName())
                    .totalOrders(summary.getTotalOrders())
                    .delayedOrders(summary.getDelayedOrders())
                    .totalDelayMinutes(summary.getTotalDelayMinutes())
                    .delayRate(summary.calculateDelayRate())
                    .build();

            responseList.add(response);
        }

        // delayRate 기준으로 내림차순 정렬
        responseList.sort(new Comparator<StoreDelaySummaryResponse>() {
            @Override
            public int compare(StoreDelaySummaryResponse o1, StoreDelaySummaryResponse o2) {
                return Double.compare(o2.getDelayRate(), o1.getDelayRate());
            }
        });

        return responseList;
    }

    /**
     * 특정 매장의 지연 통계를 조회하는 메서드
     * @param storeId 매장 ID
     * @return 해당 매장의 지연 통계 DTO
     */
    public StoreDelaySummaryResponse getStoreSummary(Long storeId) {
        StoreDelaySummary summary = summaryRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("해당 매장의 통계를 찾을 수 없습니다."));

        return StoreDelaySummaryResponse.builder()
                .storeId(summary.getStoreId())
                .storeName(summary.getStoreName())
                .totalOrders(summary.getTotalOrders())
                .delayedOrders(summary.getDelayedOrders())
                .totalDelayMinutes(summary.getTotalDelayMinutes())
                .delayRate(summary.calculateDelayRate())
                .build();
    }

    /**
     * 주어진 매장의 하루 지연 통계를 누적 반영하는 메서드
     * - 하루 동안의 완료된 주문 수, 지연된 주문 수, 지연 시간 합 등을 누적 저장
     *
     * @param storeId        매장 ID
     * @param newOrders      하루 동안 완료된 총 주문 수
     * @param newDelayed     지연된 주문 수
     * @param newDelayMinutes 지연된 주문들의 총 지연 시간 (분 단위 합)
     * @param analyzedAt     분석 기준이 되는 마지막 주문 시각
     */
    public void update(Long storeId, int newOrders, int newDelayed, long newDelayMinutes, LocalDateTime analyzedAt) {
        // 기존 통계를 조회
        StoreDelaySummary summary = summaryRepository.findByStoreId(storeId).orElse(null);

        // 없으면 오류 발생
        if (summary == null) throw new RuntimeException("통계를 찾을 수 없습니다.");

        // 누적 통계 업데이트
        summary.updateStats(newOrders, newDelayed, newDelayMinutes, analyzedAt);

        // 저장
        summaryRepository.save(summary);
    }

}
