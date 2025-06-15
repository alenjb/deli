package com.example.delivery.kafka;

import com.example.delivery.order.dto.DeliveryCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryEventProducer {

    private final KafkaTemplate<String, com.example.delivery.order.dto.DeliveryCompletedEvent> kafkaTemplate;

    public void send(String topic, DeliveryCompletedEvent event) {
        kafkaTemplate.send(topic, event);
        log.info("보낸 Kafka 메시지 → {}", event);
    }

    /**
     * 배달 완료 정보로부터 이벤트 객체를 생성하고 Kafka에 전송 하는 메서드
     *
     * @param topic       Kafka 토픽 이름
     * @param orderId     주문 ID
     * @param deliveredAt 실제 배달 완료 시간
     */
    public void send(String topic, Long orderId, LocalDateTime deliveredAt) {
        DeliveryCompletedEvent event = new DeliveryCompletedEvent(orderId, deliveredAt);
        send(topic, event);
    }
}
