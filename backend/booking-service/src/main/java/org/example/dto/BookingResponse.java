package org.example.dto;

import org.example.domain.BookingStatus;

import java.time.LocalDateTime;

public record BookingResponse(
  Long id,
  Long userId,
  Long carId,
  LocalDateTime startDate,
  LocalDateTime endDate,
  BookingStatus status,
  Float totalPrice,
  LocalDateTime createdAt
) {}
