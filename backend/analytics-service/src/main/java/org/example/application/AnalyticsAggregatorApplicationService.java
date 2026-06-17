package org.example.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.example.dto.OwnerAnalyticsSummaryResponse;
import org.example.infrastructure.client.BookingServiceClient;
import org.example.infrastructure.client.CarServiceClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsAggregatorApplicationService {

    private final CarServiceClient carServiceClient;
    private final BookingServiceClient bookingServiceClient;

    public OwnerAnalyticsSummaryResponse getOwnerAnalyticsSummary(
            String token,
            Long ownerId,
            String completedStatus,
            List<String> activeStatuses,
            LocalDateTime yearStart,
            LocalDateTime weekStart,
            LocalDateTime weekEnd) {

        CompletableFuture<Long> totalCarsFuture =
                CompletableFuture.supplyAsync(
                                () -> carServiceClient.countCarsByOwnerId(token, ownerId).getBody())
                        .exceptionally(e -> fallbackLog("totalCars", e, 0L));

        CompletableFuture<Long> totalBookingsFuture =
                CompletableFuture.supplyAsync(
                                () ->
                                        bookingServiceClient
                                                .countBookingsByOwnerId(token, ownerId)
                                                .getBody())
                        .exceptionally(e -> fallbackLog("totalBookings", e, 0L));

        CompletableFuture<Long> completedBookingsFuture =
                CompletableFuture.supplyAsync(
                                () ->
                                        bookingServiceClient
                                                .countCompletedBookingsByOwnerId(
                                                  token, ownerId, completedStatus)
                                                .getBody())
                        .exceptionally(e -> fallbackLog("completedBookings", e, 0L));

        CompletableFuture<BigDecimal> totalRevenueFuture =
                CompletableFuture.supplyAsync(
                                () ->
                                        bookingServiceClient
                                                .sumTotalPriceByOwnerIdAndStatus(
                                                  token, ownerId, completedStatus)
                                                .getBody())
                        .exceptionally(e -> fallbackLog("totalRevenue", e, BigDecimal.ZERO));

        CompletableFuture<List<Object[]>> monthlyRevenueFuture =
                CompletableFuture.supplyAsync(
                                () ->
                                        bookingServiceClient
                                                .findMonthlyRevenueByOwnerId(
                                                  token, ownerId, completedStatus, yearStart)
                                                .getBody())
                        .exceptionally(e -> fallbackLog("monthlyRevenue", e, null));

        CompletableFuture<List<Object[]>> weeklyLoadFuture =
                CompletableFuture.supplyAsync(
                                () ->
                                        bookingServiceClient
                                                .countBookedCarsByDayForOwner(
                                                  token, ownerId, activeStatuses, weekStart, weekEnd)
                                                .getBody())
                        .exceptionally(e -> fallbackLog("weeklyLoad", e, null));

        CompletableFuture.allOf(
                        totalCarsFuture,
                        totalBookingsFuture,
                        completedBookingsFuture,
                        totalRevenueFuture,
                        monthlyRevenueFuture,
                        weeklyLoadFuture)
                .join();

        return OwnerAnalyticsSummaryResponse.builder()
                .totalCars(totalCarsFuture.join())
                .totalBookings(totalBookingsFuture.join())
                .completedBookings(completedBookingsFuture.join())
                .totalRevenue(totalRevenueFuture.join())
                .monthlyRevenue(monthlyRevenueFuture.join())
                .weeklyLoad(weeklyLoadFuture.join())
                .build();
    }

    private <T> T fallbackLog(String operationName, Throwable e, T defaultValue) {
        log.error("Error fetching data for operation [{}]: {}", operationName, e.getMessage());
        return defaultValue;
    }
}
