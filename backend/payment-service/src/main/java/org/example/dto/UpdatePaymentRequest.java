package org.example.dto;

import jakarta.validation.constraints.*;
import org.example.domain.PaymentMethod;

import java.math.BigDecimal;

public record UpdatePaymentRequest(

  @Positive(message = "Amount must be positive")
  @Digits(integer = 10, fraction = 2)
  BigDecimal amount,

  PaymentMethod method
) {
}
