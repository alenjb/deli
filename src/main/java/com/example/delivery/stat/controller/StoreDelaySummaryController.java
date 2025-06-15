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

    @GetMapping("/{storeId}/delay-summary")
    public ResponseEntity<StoreDelaySummaryResponse> getStoreSummary(@PathVariable Long storeId) {
        StoreDelaySummaryResponse response = summaryService.getStoreSummary(storeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<StoreDelaySummaryResponse>> getStoreRanking() {
        List<StoreDelaySummaryResponse> ranking = summaryService.getStoreRanking();
        return ResponseEntity.ok(ranking);
    }
}
