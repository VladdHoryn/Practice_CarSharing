package org.example.infrastructure.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "booking-service",
        url = "${app.feign.booking-service-url}",
        path = "/booking/v1")
public interface BookingServiceClient {

    @GetMapping("/analytics/owners/{ownerId}/bookings")
    ResponseEntity<Long> countBookingsByOwnerId(@PathVariable("ownerId") Long ownerId);

    @GetMapping("/analytics/owners/{ownerId}/bookings/whole")
    ResponseEntity<Long> countCompletedBookingsByOwnerId(
            @PathVariable("ownerId") Long ownerId, @RequestParam("status") String status);

    @GetMapping("/analytics/owners/{ownerId}/revenue")
    ResponseEntity<BigDecimal> sumTotalPriceByOwnerIdAndStatus(
            @PathVariable("ownerId") Long ownerId, @RequestParam("status") String status);

    @GetMapping("/analytics/owners/{ownerId}/revenue/year")
    ResponseEntity<List<Object[]>> findMonthlyRevenueByOwnerId(
            @PathVariable("ownerId") Long ownerId,
            @RequestParam("status") String status,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate);

    @GetMapping("/analytics/owners/{ownerId}/load/week")
    ResponseEntity<List<Object[]>> countBookedCarsByDayForOwner(
            @PathVariable("ownerId") Long ownerId,
            @RequestParam("activeStatuses") List<String> activeStatuses,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate);
}
