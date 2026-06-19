package org.example.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnerAnalyticsSummaryResponse {
    private Long totalCars;

    private Long totalBookings;
    private Long completedBookings;
    private BigDecimal totalRevenue;

    private List<Object[]> monthlyRevenue;
    private List<Object[]> weeklyLoad;
}
