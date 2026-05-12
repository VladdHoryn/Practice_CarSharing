package org.example.dto;

import org.example.domain.CarStatus;

public record CarResponse(
        Long id,
        String brand,
        String model,
        Integer year,
        String carClass,
        Float pricePerDay,
        Long userId,
        CarStatus status,
        String imageUrl) {}
