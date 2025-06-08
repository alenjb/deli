package com.example.delivery.store.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
/* 매장 도메인 */
public class Store {

    // 매장 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 매장 이름
    private String name;

    // 매장 평균 조리 시간
    private int avgPrepMinutes;

    // 매장 주소
    private String address;

    // 매장 위도
    private double latitude;

    //매장 경도
    private double longitude;
}