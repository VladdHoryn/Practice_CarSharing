package org.example.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.example.dto.AdminAnalyticsSummaryResponse;
import org.example.dto.OwnerAnalyticsSummaryResponse;
import org.example.infrastructure.client.BookingServiceClient;
import org.example.infrastructure.client.CarServiceClient;
import org.example.infrastructure.client.UserServiceClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsAggregatorApplicationService {

    private final CarServiceClient carServiceClient;
    private final BookingServiceClient bookingServiceClient;
    private final UserServiceClient userServiceClient;

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
                                                        token,
                                                        ownerId,
                                                        activeStatuses,
                                                        weekStart,
                                                        weekEnd)
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

    public AdminAnalyticsSummaryResponse getAdminAnalyticsSummary(
            String token,
            List<String> allStatuses,
            String completedStatus,
            List<String> activeStatuses,
            LocalDateTime periodStart,
            LocalDateTime upcomingStart,
            LocalDateTime upcomingEnd,
            LocalDateTime yearStart) {

        CompletableFuture<Long> activeUsersFuture =
                CompletableFuture.supplyAsync(
                                () -> userServiceClient.countActiveUsers(token).getBody())
                        .exceptionally(e -> fallbackLog("activeUsers", e, 0L));

        CompletableFuture<Long> ownersFuture =
                CompletableFuture.supplyAsync(
                                () -> userServiceClient.countUsersByRole(token, "OWNER").getBody())
                        .exceptionally(e -> fallbackLog("ownersCount", e, 0L));

        CompletableFuture<Long> rentersFuture =
                CompletableFuture.supplyAsync(
                                () -> userServiceClient.countUsersByRole(token, "RENTER").getBody())
                        .exceptionally(e -> fallbackLog("rentersCount", e, 0L));

        CompletableFuture<Long> totalBookingsFuture =
                CompletableFuture.supplyAsync(
                                () ->
                                        bookingServiceClient
                                                .countBookingsByStatuses(token, allStatuses)
                                                .getBody())
                        .exceptionally(e -> fallbackLog("totalBookingsAdmin", e, 0L));

        CompletableFuture<BigDecimal> periodRevenueFuture =
                CompletableFuture.supplyAsync(
                                () ->
                                        bookingServiceClient
                                                .sumLastMonthRevenue(
                                                        token, completedStatus, periodStart)
                                                .getBody())
                        .exceptionally(e -> fallbackLog("periodRevenue", e, BigDecimal.ZERO));

        CompletableFuture<Long> upcomingBookingsFuture =
                CompletableFuture.supplyAsync(
                                () ->
                                        bookingServiceClient
                                                .countUpcomingBookings(
                                                        token,
                                                        activeStatuses,
                                                        upcomingStart,
                                                        upcomingEnd)
                                                .getBody())
                        .exceptionally(e -> fallbackLog("upcomingBookings", e, 0L));

        CompletableFuture<List<Object[]>> monthlyRevenueFuture =
                CompletableFuture.supplyAsync(
                                () ->
                                        bookingServiceClient
                                                .findMonthlyRevenue(
                                                        token, completedStatus, yearStart)
                                                .getBody())
                        .exceptionally(e -> fallbackLog("monthlyRevenueAdmin", e, null));

        CompletableFuture<List<Object[]>> dayOfWeekLoadFuture =
                CompletableFuture.supplyAsync(
                                () ->
                                        bookingServiceClient
                                                .countBookingsByDayOfWeek(token, activeStatuses)
                                                .getBody())
                        .exceptionally(e -> fallbackLog("dayOfWeekLoad", e, null));

        CompletableFuture.allOf(
                        activeUsersFuture,
                        ownersFuture,
                        rentersFuture,
                        totalBookingsFuture,
                        periodRevenueFuture,
                        upcomingBookingsFuture,
                        monthlyRevenueFuture,
                        dayOfWeekLoadFuture)
                .join();

        return AdminAnalyticsSummaryResponse.builder()
                .activeUsers(activeUsersFuture.join())
                .totalOwners(ownersFuture.join())
                .totalRenters(rentersFuture.join())
                .totalBookings(totalBookingsFuture.join())
                .periodRevenue(periodRevenueFuture.join())
                .upcomingBookings(upcomingBookingsFuture.join())
                .monthlyRevenue(monthlyRevenueFuture.join())
                .bookingsByDayOfWeek(dayOfWeekLoadFuture.join())
                .build();
    }

    private <T> T fallbackLog(String operationName, Throwable e, T defaultValue) {
        log.error("Error fetching data for operation [{}]: {}", operationName, e.getMessage());
        return defaultValue;
    }
}
