package org.example.dto;

import java.time.LocalDateTime;

import org.example.domain.BookingDriverStatus;

public record BookingDriverResponse(
        Long id,
        Long bookingId,
        Long userId,
        String email,
        String driverCode,
        BookingDriverStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
