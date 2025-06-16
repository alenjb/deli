package com.example.delivery.kafka;

import com.example.delivery.order.domain.Order;
import com.example.delivery.order.dto.DeliveryCompletedEvent;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.stat.service.StoreDelaySummaryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryCompletedListener {

    private final OrderRepository orderRepository;
    private final StoreDelaySummaryService summaryService;

    /**
     * Kafka로부터 배달 완료 이벤트를 수신하고 처리하는 리스너
     *
     * - 토픽: delivery-status
     * - 그룹 ID: smarteta-group
     * - 메시지 형식: DeliveryCompletedEvent (주문 ID, 배달 완료 시간 포함)
     *
     * 처리 로직:
     * 1. 전달받은 주문 ID로 Order를 조회
     * 2. 해당 Order의 배달 완료 시간(deliveredAt)을 업데이트
     * 3. StoreDelaySummaryService를 통해 지연 통계를 갱신
     */
    @KafkaListener(topics = "delivery-status", groupId = "smarteta-group")
    @Transactional
    public void listen(DeliveryCompletedEvent event) {
        log.info("📦 Kafka 메시지 수신: orderId={}, deliveredAt={}", event.orderId(), event.deliveredAt());
        StopWatch stopWatch = new StopWatch("배달 완료 처리 전체 시간");
        stopWatch.start("전체 처리");

        try {
            log.info("배달 완료 메시지 수신: {}", event);

            Optional<Order> optionalOrder = orderRepository.findById(event.orderId());
            if (optionalOrder.isEmpty()) {
                log.warn("주문 ID={} 에 해당하는 주문을 찾을 수 없습니다.", event.orderId());
                return;
            }

            Order order = optionalOrder.get();
            order.completeDelivery(event.deliveredAt());
            summaryService.processCompletedOrder(order);

            log.info("주문 처리 완료 → 주문 ID: {}", order.getId());
        } catch (Exception e){
            log.error(" Kafka 메시지 처리 중 예외 발생", e);
        } finally {
            stopWatch.stop();
            log.info("[성능측정] Kafka 이벤트 전체 처리 시간: {}ms", stopWatch.getTotalTimeMillis());
        }
    }
}
