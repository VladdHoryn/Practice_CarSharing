package org.example.dto;

import java.time.LocalDateTime;

public record CarAvailabilityResponse(LocalDateTime startDate, LocalDateTime endDate) {}
