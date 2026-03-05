package com.finsync.core.controller;

import com.finsync.core.dto.CategoryStatResponse;
import com.finsync.core.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Returns the spending breakdown by category for the current billing period of the given card.
     * Used to power the donut charts in the frontend dashboard.
     */
    @GetMapping("/cards/{cardId}/categories")
    public ResponseEntity<List<CategoryStatResponse>> getCategoryStats(@PathVariable UUID cardId) {
        return ResponseEntity.ok(statisticsService.getCategoryStats(cardId));
    }
}
