package com.example.delivery.order.domain;

/**
 * 배달 상태를 나타내는 enum
 * 주문이 현재 어떤 단계에 있는지 명확히 표현하기 위해 사용됨
 */
public enum DeliveryStatus {
    ASSIGNED("배달 준비 중"),    // 라이더에게 배정 상태
    PICKED_UP("배달 중"),       // 라이더가 음식 픽업 완료 상태
    DELIVERED("배달 완료");     // 고객에게 배달 완료 상태

    private final String message;

    DeliveryStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
