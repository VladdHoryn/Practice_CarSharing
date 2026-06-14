package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBookingDriverRequest(
  @NotBlank String email,
  @NotBlank String driverCode
) {
}
