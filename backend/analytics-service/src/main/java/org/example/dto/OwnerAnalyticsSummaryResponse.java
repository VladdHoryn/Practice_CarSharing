package org.example.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnerAnalyticsSummaryResponse {
    // Дані з car-service
    private Long totalCars;

    // Дані з booking-service
    private Long totalBookings;
    private Long completedBookings;
    private BigDecimal totalRevenue;

    private List<Object[]> monthlyRevenue;
    private List<Object[]> weeklyLoad;
}
