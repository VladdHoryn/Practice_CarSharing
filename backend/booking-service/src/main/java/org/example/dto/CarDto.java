package org.example.dto;

import lombok.Getter;
import lombok.Setter;

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
