package org.example.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAnalyticsSummaryResponse {
    // Дані з user-service
    private Long activeUsers;
    private Long totalOwners;
    private Long totalRenters;

    // Дані з booking-service
    private Long totalBookings;
    private BigDecimal periodRevenue;
    private Long upcomingBookings;
    private List<Object[]> monthlyRevenue;
    private List<Object[]> bookingsByDayOfWeek;
}
