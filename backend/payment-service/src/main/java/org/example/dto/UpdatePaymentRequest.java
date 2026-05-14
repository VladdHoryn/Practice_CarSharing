package org.example.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

import org.example.domain.PaymentMethod;

public record UpdatePaymentRequest(
        @Positive(message = "Amount must be positive") @Digits(integer = 10, fraction = 2)
                BigDecimal amount,
        PaymentMethod method,
        String currency) {}
