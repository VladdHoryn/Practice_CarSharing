package org.example.dto;

import java.time.LocalDateTime;
import org.example.domain.BookingStatus;

public record BookingResponse(
        Long id,
        Long userId,
        Long carId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BookingStatus status,
        Float totalPrice,
        LocalDateTime createdAt) {}
