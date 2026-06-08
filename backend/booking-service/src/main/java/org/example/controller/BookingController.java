package org.example.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.example.application.BookingApplicationService;
import org.example.domain.Booking;
import org.example.dto.BookingResponse;
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

    // CREATED → PENDING
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{id}/submit")
    public ResponseEntity<BookingResponse> submitBooking(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(bookingService.submitBooking(id)));
    }

    // PENDING → CONFIRMED
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(bookingService.confirmBooking(id)));
    }

    // ANY → CANCELLED
    @PreAuthorize("hasAnyRole('RENTER', 'ADMINISTRATOR')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(bookingService.cancelBooking(id)));
    }

    // CONFIRMED → COMPLETED
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(bookingService.completeBooking(id)));
    }

  @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
