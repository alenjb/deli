package com.example.delivery.eta.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EtaUpdatedEvent {
    private Long orderId;
    private Long memberId;
    private LocalDateTime newEta;
}
