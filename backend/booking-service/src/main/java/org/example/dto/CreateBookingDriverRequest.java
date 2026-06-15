package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBookingDriverRequest(@NotBlank String email, @NotBlank String driverCode) {}
