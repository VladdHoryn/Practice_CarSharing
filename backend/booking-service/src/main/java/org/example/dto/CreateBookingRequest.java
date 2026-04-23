package org.example.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateBookingRequest(
  @NotNull Long userId,
  @NotNull Long carId,
  @NotNull LocalDateTime startDate,
  @NotNull LocalDateTime endDate,
  @NotNull Float pricePerDay
) {}
