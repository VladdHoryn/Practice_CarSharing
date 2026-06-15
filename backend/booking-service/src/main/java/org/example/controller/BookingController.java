package org.example.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.example.application.BookingApplicationService;
import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.example.dto.BookingResponse;
import org.example.dto.BookingStatusChange;
import org.example.dto.CreateBookingRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/booking/v1")
@RequiredArgsConstructor
public class BookingController {
    private final BookingApplicationService bookingService;

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getCarId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus(),
                booking.getTotalPrice(),
                booking.getCancelDeadline(),
                booking.getCreatedAt(),
                booking.getUpdatedAt());
    }

    @PreAuthorize("hasAnyRole('RENTER', 'ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody @Valid CreateBookingRequest request) {

        Booking booking =
                bookingService.createBooking(
                        request.userId(),
                        request.carId(),
                        request.startDate(),
                        request.endDate(),
                        request.pricePerDay());

        return ResponseEntity.created(URI.create("/booking/v1/" + booking.getId()))
                .body(toResponse(booking));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(bookingService.getBookingById(id)));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(
                bookingService.getAllBookings().stream().map(this::toResponse).toList());
    }

    @PreAuthorize("hasAnyRole('RENTER', 'ADMINISTRATOR')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(
                bookingService.getUserBookings(userId).stream().map(this::toResponse).toList());
    }

    // ANY → CANCELLED
    @PreAuthorize("hasAnyRole('RENTER')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(bookingService.cancelBooking(id)));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{id}/status/change")
    public ResponseEntity<Void> changeBookingStatus(
            @PathVariable Long id, @RequestBody BookingStatusChange request) {
        bookingService.changeStatus(id, request.newStatus());

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    // OWNER ANALYTICS

    @GetMapping("analytics/owner/bookings")
    public long countBookingsByOwnerId(Long ownerId){
      return bookingService.countBookingsByOwnerId(ownerId);
    }

    @GetMapping("analytics/owner/bookings/whole")
    public long countCompletedBookingsByOwnerId(Long ownerId, BookingStatus status){
      return bookingService.countCompletedBookingsByOwnerId(ownerId, status);
    }

    @GetMapping("analytics/owner/revenue")
    public double sumTotalPriceByOwnerIdAndStatus(Long ownerId, BookingStatus status){
      return bookingService.sumTotalPriceByOwnerIdAndStatus(ownerId, status);
    }

    @GetMapping("analytics/owner/revenue/year")
    public List<Object[]> findMonthlyRevenueByOwnerId(Long ownerId, BookingStatus status, LocalDateTime startDate){
      return bookingService.findMonthlyRevenueByOwnerId(ownerId, status, startDate);
    }

    @GetMapping("analytics/owner/load/week")
    public List<Object[]> countBookedCarsByDayForOwner(Long ownerId, List<BookingStatus> activeStatuses,
                                                       LocalDateTime startDate, LocalDateTime endDate){
      return bookingService.countBookedCarsByDayForOwner(ownerId, activeStatuses, startDate, endDate);
    }
}
