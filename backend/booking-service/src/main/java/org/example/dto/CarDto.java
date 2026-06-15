package org.example.dto;

public record CarDto(
  Long id,
  String brand,
  String model,
  Integer year,
  String carClass,
  Float pricePerDay,
  Long userId,
  String status,
  String imageUrl
) {
}
