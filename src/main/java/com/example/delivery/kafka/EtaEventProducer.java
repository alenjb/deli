package com.example.delivery.kafka;

import com.example.delivery.eta.dto.EtaUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EtaEventProducer {

    private final KafkaTemplate<String, EtaUpdatedEvent> kafkaTemplate;
    private static final String TOPIC = "eta-updated";

    public void sendEtaUpdatedEvent(EtaUpdatedEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }
}