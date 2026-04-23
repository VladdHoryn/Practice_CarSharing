package org.example.dto;

import jakarta.validation.constraints.NotNull;

public record RentCarRequest(
  @NotNull
  Long userId
) {}
