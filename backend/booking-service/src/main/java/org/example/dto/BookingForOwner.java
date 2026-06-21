package org.example.dto;

import org.example.domain.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingForOwner(
  Long id,
  Long carId,
  LocalDateTime startDate,
  LocalDateTime endDate,
  BookingStatus status,
  BigDecimal totalPrice,
  LocalDateTime cancelDeadline,
  LocalDateTime createdAt,
  LocalDateTime updatedAt) {}
