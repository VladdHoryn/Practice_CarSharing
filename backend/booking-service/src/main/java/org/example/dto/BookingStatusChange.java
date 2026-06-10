package org.example.dto;

import jakarta.validation.constraints.NotNull;
import org.example.domain.BookingStatus;

public record BookingStatusChange(
  @NotNull BookingStatus newStatus
) {
}
