package org.example.dto;

import org.example.domain.BookingDriverStatus;

import java.time.LocalDateTime;

public record BookingDriverResponse(
  Long id,
  Long bookingId,
  Long userId,
  String email,
  String driverCode,
  BookingDriverStatus status,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
}
