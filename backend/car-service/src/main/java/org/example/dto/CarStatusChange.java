package org.example.dto;

import jakarta.validation.constraints.NotNull;

import org.example.domain.CarStatus;

public record CarStatusChange(@NotNull CarStatus newStatus) {}
