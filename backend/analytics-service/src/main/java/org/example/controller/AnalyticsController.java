package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.application.AnalyticsAggregatorApplicationService;
import org.example.dto.OwnerAnalyticsSummaryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

  private final AnalyticsAggregatorApplicationService analyticsAggregatorService;

  @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
  @GetMapping("/owners/{ownerId}/summary")
  public ResponseEntity<OwnerAnalyticsSummaryResponse> getSummary(
    @PathVariable Long ownerId,
    @RequestParam(defaultValue = "COMPLETED") String completedStatus,
    @RequestParam(defaultValue = "ACTIVE,IN_PROGRESS") List<String> activeStatuses,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime yearStart,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime weekStart,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime weekEnd) {

    OwnerAnalyticsSummaryResponse summary = analyticsAggregatorService.getOwnerAnalyticsSummary(
      ownerId, completedStatus, activeStatuses, yearStart, weekStart, weekEnd
    );

    return ResponseEntity.ok(summary);
  }
}
