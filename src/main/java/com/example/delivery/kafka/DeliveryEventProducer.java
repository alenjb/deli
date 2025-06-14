package com.example.delivery.kafka;

import com.example.delivery.order.dto.DeliveryCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryEventProducer {

    private final KafkaTemplate<String, com.example.delivery.order.dto.DeliveryCompletedEvent> kafkaTemplate;

    public void send(String topic, DeliveryCompletedEvent event) {
        kafkaTemplate.send(topic, event);
        log.info("보낸 Kafka 메시지 → {}", event);
    }
}
