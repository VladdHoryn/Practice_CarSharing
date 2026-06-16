package org.example.dto;

import jakarta.validation.constraints.*;

public record CreateCarRequest(
        @NotBlank @Size(min = 2, max = 50) String brand,
        @NotBlank @Size(min = 1, max = 50) String model,
        @NotNull @Min(1950) Integer year,
        @NotNull String carClass,
        @NotNull @Positive Float pricePerDay,
        @Pattern(regexp = "^(https?://.*)?$") String imageUrl,
        @NotNull Long userId) {}
