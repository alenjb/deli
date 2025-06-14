package com.example.delivery.kafka;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaTestController {

    private final DeliveryEventProducer producer;

    public KafkaTestController(DeliveryEventProducer producer) {
        this.producer = producer;
    }

    @GetMapping("/send")
    public ResponseEntity<String> send() {
        producer.send("test-topic", "스프링과 카프카 연동 완료!");
        return ResponseEntity.ok("Sent!");
    }
}
