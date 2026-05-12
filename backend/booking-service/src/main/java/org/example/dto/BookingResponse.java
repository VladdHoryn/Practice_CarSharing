package org.example.dto;

import org.example.domain.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
  Long id,
  Long userId,
  Long carId,
  LocalDateTime startDate,
  LocalDateTime endDate,
  BookingStatus status,
  BigDecimal totalPrice,
  LocalDateTime createdAt
) {}
