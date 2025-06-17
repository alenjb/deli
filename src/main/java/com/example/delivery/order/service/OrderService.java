package com.example.delivery.order.service;

import com.example.delivery.eta.dto.EtaUpdatedEvent;
import com.example.delivery.kafka.DeliveryEventProducer;
import com.example.delivery.kafka.EtaEventProducer;
import com.example.delivery.order.domain.DeliveryStatus;
import com.example.delivery.order.domain.Order;
import com.example.delivery.order.dto.DeliveryCompletedEvent;
import com.example.delivery.order.dto.OrderRequest;
import com.example.delivery.order.dto.OrderResponse;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.store.domain.Store;
import com.example.delivery.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    public static final double PEAK_TIME_MULTIPLIER = 1.2; // 피크 시간대 가중치
    private final OrderRepository orderRepository;
    private final StoreRepository  storeRepository;
    private final DeliveryEventProducer producer;
    private final EtaEventProducer etaEventProducer;


    /**
     * 주문을 생성하는 메서드
     * @param request 주문 요청 DTO
     * @return 주문 응답 DTO
     */
    public OrderResponse makeOrder(OrderRequest request) {
        Optional<Store> store = storeRepository.findById(request.getStoreId());
        if(store.isEmpty()) throw new RuntimeException("매장을 찾을 수 없습니다.");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eta = calculateETA(store.get(), request, now);

        Order order = Order.builder()
                .userId(request.getUserId())
                .store(store.get())
                .distanceKm(request.getDistanceKm())
                .createdAt(now)
                .eta(eta)
                .status(DeliveryStatus.ASSIGNED) // 기본 상태
                .build();

        orderRepository.save(order);

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getDeliveredAt(),
                order.getStore().getId()
        );
    }

    /**
     * 배달을 완료하는 메서드
     * @param orderId 주문 ID
     * @param deliveredAt 배달 완료 시각
     */
    public void completeDelivery(Long orderId, LocalDateTime deliveredAt) {
        Optional<Order> order = orderRepository.findById(orderId);
        if(order.isEmpty()) throw new RuntimeException("주문을 찾을 수 없습니다.");

        Order o = order.get();
        o.completeDelivery(deliveredAt);
        orderRepository.save(o);

        // Kafka 메시지 발행
        DeliveryCompletedEvent event = new DeliveryCompletedEvent(o.getId(), deliveredAt);
        producer.send("delivery-status", event);
    }

    /**
     * ETA를 계산하는 메서드
     * @param store 매장 정보
     * @param request 주문 요청 DTO
     * @param now 현재 시각
     * @return 계산된 ETA
     */
    public LocalDateTime calculateETA(Store store, OrderRequest request, LocalDateTime now) {
        int prepTime = isPeakTime(now) ? (int) ( store.getAvgPrepMinutes() * PEAK_TIME_MULTIPLIER) : store.getAvgPrepMinutes();
        int totalMinutes = prepTime + request.getEstimatedDeliveryTimeMinutes();
        return now.plusMinutes(totalMinutes);
    }

    /**
     *  점싱 혹은 저녁 시간인지 판단하는 메서드
     *  점심 시간: 11 ~ 13시
     *  저녁 시간: 18 ~ 20시
     * @param time 시간
     * @return 점심 혹은 저녁 시간 여부
     */
    private boolean isPeakTime(LocalDateTime time) {
        int hour = time.getHour();
        return (hour >= 11 && hour <= 13) || (hour >= 18 && hour <= 20);
    }

    /**
     * 매장의 요청에 따라 ETA를 조정하는 메서드
     * 매장이 ETA를 몇 분 연장해달라고 요청한 경우 사용
     * @param orderId 주문 ID
     * @param additionalMinutes ETA에 추가할 분(minute) 단위 시간
     */
    public void adjustEtaByStore(Long orderId, int additionalMinutes) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        // 기존 ETA에 요청받은 시간만큼 추가
        order.setEta(order.getEta().plusMinutes(additionalMinutes));
        orderRepository.save(order);

        // Kafka로 ETA 변경 이벤트 발행
        etaEventProducer.sendEtaUpdatedEvent(new EtaUpdatedEvent(
                order.getId(), order.getUserId(), order.getEta()));
    }

    /**
     * 조리 완료 시 ETA를 현재 시각을 기준으로 재설정하는 메서드
     * 평균 배달 시간을 기준으로 ETA를 재조정
     * @param orderId 주문 ID
     */
    public void adjustEtaOnCookingCompleted(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        // 현재 시간 기준 ETA 재계산
        order.setEta(LocalDateTime.now().plusMinutes(20));
        orderRepository.save(order);

        // Kafka로 ETA 변경 이벤트 발행
        etaEventProducer.sendEtaUpdatedEvent(new EtaUpdatedEvent(
                order.getId(), order.getUserId(), order.getEta()));
    }
}
