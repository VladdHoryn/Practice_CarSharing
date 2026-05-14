package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public record MarkAsProcessingRequest(

  @NotBlank(message = "Provider payment id is required")
  String providerPaymentId,

  @NotBlank(message = "Client secret is required")
  String clientSecret
) {
}
