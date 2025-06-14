package com.example.delivery.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventListener {

    @KafkaListener(topics = "test-topic", groupId = "smarteta-group")
    public void listen(String message){
        System.out.println("받은 카프카 메시지: " + message);
    }
}
