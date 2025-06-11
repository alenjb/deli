package com.example.delivery.order.repository;

import com.example.delivery.order.domain.Order;
import com.example.delivery.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    /**
     * 특정 매장에서 특정 시점 이후 생성된 주문만 가져오는 메서드
     * @param store 매장
     * @param createdAt 특정 시점
     * @return 특정 시점 이후의 주문들
     */
    List<Order> findByStoreAndCreatedAtAfter(Store store, LocalDateTime createdAt);

}
