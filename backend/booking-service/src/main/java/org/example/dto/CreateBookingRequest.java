package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull Long userId,
        @NotNull Long carId,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate,
        @NotNull BigDecimal pricePerDay) {}
