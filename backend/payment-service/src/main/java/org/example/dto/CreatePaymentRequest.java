package org.example.dto;

import jakarta.validation.constraints.*;
import org.example.domain.PaymentMethod;

import java.math.BigDecimal;

public record CreatePaymentRequest(

        @NotNull(message = "Booking id is required")
        Long bookingId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        PaymentMethod method,

        @NotBlank(message = "Currency is required")
        String currency
) {
}
