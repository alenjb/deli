package com.example.delivery.stat.controller;

import com.example.delivery.stat.dto.StoreDelaySummaryResponse;
import com.example.delivery.stat.service.StoreDelaySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreDelaySummaryController {

    private final StoreDelaySummaryService summaryService;

    /**
     * 특정 매장의 지연 통계를 조회하는 API
     * @param storeId 매장 ID
     * @return 해당 매장의 지연 통계
     */
    @GetMapping("/{storeId}/delay-summary")
    public ResponseEntity<StoreDelaySummaryResponse> getStoreSummary(@PathVariable Long storeId) {
        StoreDelaySummaryResponse response = summaryService.getStoreSummary(storeId);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 매장 중 지연률 기준 순위 목록을 반환하는 API
     * @return 매장별 지연률 내림차순 리스트
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<StoreDelaySummaryResponse>> getStoreRanking() {
        List<StoreDelaySummaryResponse> ranking = summaryService.getStoreRanking();
        return ResponseEntity.ok(ranking);
    }
}
