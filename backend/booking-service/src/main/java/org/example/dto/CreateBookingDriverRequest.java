package org.example.dto;

public record CreateBookingDriverRequest(
  String email,
  String driverCode
) {
}
