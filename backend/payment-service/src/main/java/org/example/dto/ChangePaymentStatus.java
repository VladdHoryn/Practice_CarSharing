package org.example.dto;

import jakarta.validation.constraints.NotNull;

import org.example.domain.PaymentStatus;

public record ChangePaymentStatus(@NotNull PaymentStatus newStatus) {}
