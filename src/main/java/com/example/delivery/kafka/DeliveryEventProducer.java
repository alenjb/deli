package com.example.delivery.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeliveryEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public DeliveryEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("보낸 카프카 메시지 → " + message);
    }
}

