package org.example.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.OwnerAnalyticsSummaryResponse;
import org.example.infrastructure.client.BookingServiceClient;
import org.example.infrastructure.client.CarServiceClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsAggregatorApplicationService {

  private final CarServiceClient carServiceClient;
  private final BookingServiceClient bookingServiceClient;

  public OwnerAnalyticsSummaryResponse getOwnerAnalyticsSummary(
    Long ownerId,
    String completedStatus,
    List<String> activeStatuses,
    LocalDateTime yearStart,
    LocalDateTime weekStart,
    LocalDateTime weekEnd) {

    // 1. Запускаємо всі запити асинхронно
    CompletableFuture<Long> totalCarsFuture = CompletableFuture.supplyAsync(() ->
        carServiceClient.countCarsByOwnerId(ownerId).getBody())
      .exceptionally(e -> fallbackLog("totalCars", e, 0L));

    CompletableFuture<Long> totalBookingsFuture = CompletableFuture.supplyAsync(() ->
        bookingServiceClient.countBookingsByOwnerId(ownerId).getBody())
      .exceptionally(e -> fallbackLog("totalBookings", e, 0L));

    CompletableFuture<Long> completedBookingsFuture = CompletableFuture.supplyAsync(() ->
        bookingServiceClient.countCompletedBookingsByOwnerId(ownerId, completedStatus).getBody())
      .exceptionally(e -> fallbackLog("completedBookings", e, 0L));

    CompletableFuture<BigDecimal> totalRevenueFuture = CompletableFuture.supplyAsync(() ->
        bookingServiceClient.sumTotalPriceByOwnerIdAndStatus(ownerId, completedStatus).getBody())
      .exceptionally(e -> fallbackLog("totalRevenue", e, BigDecimal.ZERO));

    CompletableFuture<List<Object[]>> monthlyRevenueFuture = CompletableFuture.supplyAsync(() ->
        bookingServiceClient.findMonthlyRevenueByOwnerId(ownerId, completedStatus, yearStart).getBody())
      .exceptionally(e -> fallbackLog("monthlyRevenue", e, null));

    CompletableFuture<List<Object[]>> weeklyLoadFuture = CompletableFuture.supplyAsync(() ->
        bookingServiceClient.countBookedCarsByDayForOwner(ownerId, activeStatuses, weekStart, weekEnd).getBody())
      .exceptionally(e -> fallbackLog("weeklyLoad", e, null));

    // 2. Чекаємо завершення всіх потоків
    CompletableFuture.allOf(
      totalCarsFuture, totalBookingsFuture, completedBookingsFuture,
      totalRevenueFuture, monthlyRevenueFuture, weeklyLoadFuture
    ).join();

    // 3. Збираємо фінальний результат
    return OwnerAnalyticsSummaryResponse.builder()
      .totalCars(totalCarsFuture.join())
      .totalBookings(totalBookingsFuture.join())
      .completedBookings(completedBookingsFuture.join())
      .totalRevenue(totalRevenueFuture.join())
      .monthlyRevenue(monthlyRevenueFuture.join())
      .weeklyLoad(weeklyLoadFuture.join())
      .build();
  }

  // Метод для обробки помилок і повернення дефолтного значення
  private <T> T fallbackLog(String operationName, Throwable e, T defaultValue) {
    log.error("Error fetching data for operation [{}]: {}", operationName, e.getMessage());
    return defaultValue;
  }
}
