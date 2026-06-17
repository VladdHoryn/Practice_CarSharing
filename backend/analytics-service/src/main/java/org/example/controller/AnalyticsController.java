package org.example.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.example.application.AnalyticsAggregatorApplicationService;
import org.example.dto.AdminAnalyticsSummaryResponse;
import org.example.dto.OwnerAnalyticsSummaryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsAggregatorApplicationService analyticsAggregatorService;

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/owners/{ownerId}/summary")
    public ResponseEntity<OwnerAnalyticsSummaryResponse> getSummary(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable Long ownerId,
            @RequestParam(defaultValue = "COMPLETED") String completedStatus,
            @RequestParam(defaultValue = "ACTIVE,IN_PROGRESS") List<String> activeStatuses,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime yearStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime weekStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime weekEnd) {

        OwnerAnalyticsSummaryResponse summary =
                analyticsAggregatorService.getOwnerAnalyticsSummary(
                        token,
                        ownerId,
                        completedStatus,
                        activeStatuses,
                        yearStart,
                        weekStart,
                        weekEnd);

        return ResponseEntity.ok(summary);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/admin/summary")
    public ResponseEntity<AdminAnalyticsSummaryResponse> getAdminSummary(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(defaultValue = "PENDING,CONFIRMED,COMPLETED,CANCELED")
                    List<String> allStatuses,
            @RequestParam(defaultValue = "COMPLETED") String completedStatus,
            @RequestParam(defaultValue = "CONFIRMED,PENDING") List<String> activeStatuses,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime upcomingStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime upcomingEnd,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime yearStart) {

        LocalDateTime now = LocalDateTime.now();
        if (periodStart == null) periodStart = now.minusDays(30);
        if (upcomingStart == null) upcomingStart = now;
        if (upcomingEnd == null) upcomingEnd = now.plusDays(30);
        if (yearStart == null)
            yearStart = now.withDayOfYear(1).withHour(0).withMinute(0); // Початок поточного року

        AdminAnalyticsSummaryResponse summary =
                analyticsAggregatorService.getAdminAnalyticsSummary(
                        token,
                        allStatuses,
                        completedStatus,
                        activeStatuses,
                        periodStart,
                        upcomingStart,
                        upcomingEnd,
                        yearStart);

        return ResponseEntity.ok(summary);
    }
}
