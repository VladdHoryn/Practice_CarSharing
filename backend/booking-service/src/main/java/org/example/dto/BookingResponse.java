package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.example.domain.BookingStatus;

public record BookingResponse(
        Long id,
        Long userId,
        Long carId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BookingStatus status,
        BigDecimal totalPrice,
        LocalDateTime cancelDeadline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
