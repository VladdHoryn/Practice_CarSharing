package org.example.dto;

public record CreateBookingDriverRequest(
  Long userId,
  String email,
  String driverCode
) {
}
