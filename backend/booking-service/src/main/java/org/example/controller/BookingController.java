package org.example.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.example.application.BookingApplicationService;
import org.example.application.BookingDriverApplicationService;
import org.example.domain.Booking;
import org.example.domain.BookingDriver;
import org.example.domain.BookingStatus;
import org.example.dto.BookingResponse;
import org.example.dto.BookingStatusChange;
import org.example.dto.CreateBookingRequest;
import org.example.dto.*;
import org.example.exception.UserWasNotFound;
import org.example.infrastructure.client.UserServiceClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/booking/v1")
@RequiredArgsConstructor
public class BookingController {
    private final BookingApplicationService bookingService;
    private final BookingDriverApplicationService bookingDriverService;
    private final UserServiceClient userServiceClient;

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

    private BookingDriverResponse bookingDriverToResponse(BookingDriver bookingDriver) {
        return new BookingDriverResponse(
                bookingDriver.getId(),
                bookingDriver.getBookingId(),
                bookingDriver.getUserId(),
                bookingDriver.getEmail(),
                bookingDriver.getDriverCode(),
                bookingDriver.getStatus(),
                bookingDriver.getCreatedAt(),
                bookingDriver.getUpdatedAt());
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

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/analytics/owners/{ownerId}/bookings")
    public ResponseEntity<Long> countBookingsByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(bookingService.countBookingsByOwnerId(ownerId));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/analytics/owners/{ownerId}/bookings/whole")
    public ResponseEntity<Long> countCompletedBookingsByOwnerId(
            @PathVariable Long ownerId, @RequestParam BookingStatus status) {
        return ResponseEntity.ok(bookingService.countCompletedBookingsByOwnerId(ownerId, status));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/analytics/owners/{ownerId}/revenue")
    public ResponseEntity<BigDecimal> sumTotalPriceByOwnerIdAndStatus(
            @PathVariable Long ownerId, @RequestParam BookingStatus status) {
        return ResponseEntity.ok(bookingService.sumTotalPriceByOwnerIdAndStatus(ownerId, status));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/analytics/owners/{ownerId}/revenue/year")
    public ResponseEntity<List<Object[]>> findMonthlyRevenueByOwnerId(
            @PathVariable Long ownerId,
            @RequestParam BookingStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate) {
        return ResponseEntity.ok(
                bookingService.findMonthlyRevenueByOwnerId(ownerId, status, startDate));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/analytics/owners/{ownerId}/load/week")
    public ResponseEntity<List<Object[]>> countBookedCarsByDayForOwner(
            @PathVariable Long ownerId,
            @RequestParam List<BookingStatus> activeStatuses,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate) {
        return ResponseEntity.ok(
                bookingService.countBookedCarsByDayForOwner(
                        ownerId, activeStatuses, startDate, endDate));
    }

    @PreAuthorize("hasAnyRole('RENTER')")
    @PostMapping("/{bookingId}/drivers")
    public ResponseEntity<BookingDriverResponse> createInvitation(
            @PathVariable Long bookingId, @RequestBody @Valid CreateBookingDriverRequest request) {

        Optional<Long> userId =
                userServiceClient.existByEmailAndDriverCode(request.email(), request.driverCode());

        if (!userId.isPresent()) {
            throw new UserWasNotFound(
                    "User with email="
                            + request.email()
                            + " and driverCode="
                            + request.driverCode()
                            + " was not found");
        }

        BookingDriver bookingDriver =
                bookingDriverService.createInvitation(
                        bookingId, userId.get(), request.email(), request.driverCode());

        return ResponseEntity.created(
                        URI.create(
                                "/booking/v1/" + bookingId + "/drivers/" + bookingDriver.getId()))
                .body(this.bookingDriverToResponse(bookingDriver));
    }

    @PreAuthorize("hasAnyRole('RENTER')")
    @PostMapping("/drivers/{invitationId}/accept")
    public ResponseEntity<BookingDriverResponse> acceptInvitation(@PathVariable Long invitationId) {

        return ResponseEntity.ok(
                bookingDriverToResponse(bookingDriverService.acceptInvitation(invitationId)));
    }

    @PreAuthorize("hasAnyRole('RENTER')")
    @PostMapping("/drivers/{invitationId}/decline")
    public ResponseEntity<BookingDriverResponse> declineInvitation(
            @PathVariable Long invitationId) {

        return ResponseEntity.ok(
                bookingDriverToResponse(bookingDriverService.declineInvitation(invitationId)));
    }

    @GetMapping("/drivers/{userId}")
    public ResponseEntity<List<BookingDriverResponse>> getInvitationsByUserId(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                bookingDriverService.getByUserId(userId).stream()
                        .map(bookingDriver -> bookingDriverToResponse(bookingDriver))
                        .toList());
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<BookingDriverResponse>> getAllInvitations() {
        return ResponseEntity.ok(
                bookingDriverService.getAll().stream()
                        .map(bookingDriver -> bookingDriverToResponse(bookingDriver))
                        .toList());
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/analytics/admin/bookings/count")
    public ResponseEntity<Long> countBookingsByStatuses(
            @RequestParam List<BookingStatus> statuses) {
        return ResponseEntity.ok(bookingService.countBookingsByStatuses(statuses));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/analytics/admin/revenue/period")
    public ResponseEntity<BigDecimal> sumLastMonthRevenue(
            @RequestParam BookingStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate) {
        return ResponseEntity.ok(bookingService.sumLastMonthRevenue(status, startDate));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/analytics/admin/bookings/upcoming")
    public ResponseEntity<Long> countUpcomingBookings(
            @RequestParam List<BookingStatus> activeStatuses,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate) {
        return ResponseEntity.ok(
                bookingService.countUpcomingBookings(activeStatuses, startDate, endDate));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/analytics/admin/revenue/monthly")
    public ResponseEntity<List<Object[]>> findMonthlyRevenue(
            @RequestParam BookingStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate) {
        return ResponseEntity.ok(bookingService.findMonthlyRevenue(status, startDate));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/analytics/admin/load/day-of-week")
    public ResponseEntity<List<Object[]>> countBookingsByDayOfWeek(
            @RequestParam List<BookingStatus> activeStatuses) {
        return ResponseEntity.ok(bookingService.countBookingsByDayOfWeek(activeStatuses));
    }
}
