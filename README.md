# 🚚 SmartETA - 상황 기반 ETA 조정 및 통계 시스템

> 조리 완료, 매장 요청, 누적 지연률에 따라 ETA(예상 도착 시간)를 유동적으로 조정하고,  
Kafka를 통해 고객에게 실시간으로 ETA 변경을 알리는 **이벤트 기반 ETA 시스템**입니다.

---

## 🧭 프로젝트 개요

배달 시스템에서 ETA(예상 도착 시간)가 어떻게 설계되고 동작하는지를 직접 구현하며 학습해보고자 시작한 프로젝트입니다.
조리 상태, 매장 요청, 누적 지연률 등 다양한 상황에 따라 ETA가 어떻게 변화하는지를 시뮬레이션하고,
Kafka를 이용해 변경된 ETA를 외부 시스템에 전달하는 흐름까지 구현했습니다.

---

## ⚙️ 주요 기능 요약

### ✅ ETA 조정 트리거

| 시점 | 조정 내용 |
|------|-----------|
| 주문 접수 | ETA = 현재 시간 + **매장별 예상 조리/대기 시간** |
| 매장 요청 | ETA += 매장 요청 시간 |
| 조리 완료 | ETA = 현재 시간 + 평균 배달 시간 (20분- 외부에서 값이 주어진다고 가정) |
| 매장별 예상 조리/대기 시간 업데이트 | 하루 동안 누적된 지연률을 바탕으로 매장별 ETA 기준 보정 |

---

### ✅ Kafka 이벤트 발행 구조

ETA가 변경되면 `eta-updated` 토픽으로 Kafka 메시지 발행:

```json
{
  "orderId": 123,
  "userId": 45,
  "newEta": "2025-06-18T13:45:00"
}
```

# 📂 패키지 구조

```
com.example.delivery
├── DeliveryApplication.java
│
├── eta
│   ├── domain                  → EtaHistory.java
│   ├── dto                     → EtaUpdatedEvent.java
│   └── scheduler               → EtaScheduler.java
│
├── kafka
│   ├── DeliveryCompletedListener.java
│   ├── DeliveryEventProducer.java
│   └── EtaEventProducer.java
│
├── order
│   ├── controller              → OrderController.java
│   ├── domain                  → Order.java, DeliveryStatus.java
│   ├── dto                     → OrderRequest, OrderResponse, DeliveryCompletedEvent
│   ├── repository              → OrderRepository.java
│   └── service                 → OrderService.java
│
├── stat
│   ├── controller              → StoreDelaySummaryController.java
│   ├── domain                  → StoreDelaySummary.java
│   ├── dto                     → StoreDelaySummaryResponse.java
│   ├── repository              → StoreDelaySummaryRepository.java
│   └── service                 → StoreDelaySummaryService.java
```

# 📚 주요 API

| 메서드    | URL                          | 설명                    |
| ------ | ---------------------------- | --------------------- |
| `POST` | `/orders`                    | 주문 생성                 |
| `POST` | `/orders/{orderId}/confirm`  | 주문 확정 (조리 시작 등)       |
| `POST` | `/orders/{orderId}/complete` | 조리 완료 (ETA 자동 조정 트리거) |
| `GET` | `/stores/{storeId}/delay-summary` | 해당 매장의 누적 지연 통계 조회    |
| `GET` | `/stores/ranking`                 | 지연률 기준 상위 랭킹 매장 목록 조회 |


# 🛠 기술 스택
| 분야              | 기술                              |
| --------------- | ------------------------------- |
| Language        | Java 17                         |
| Framework       | Spring Boot 3                   |
| DB & ORM        | MySQL, Spring Data JPA          |
| Messaging       | Apache Kafka                    |
| Task Scheduling | Spring Scheduler (`@Scheduled`) |
| Containerization | Docker (Kafka, 애플리케이션 실행 환경 구성) |
| API 문서화         | Swagger             |
| 빌드              | Gradle                          |

