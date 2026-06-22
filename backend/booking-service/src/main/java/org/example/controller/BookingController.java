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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/booking/v1")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management and analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
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

    private BookingForOwner toResponseForOwner(Booking booking) {
        return new BookingForOwner(
                booking.getId(),
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

    @Operation(
            summary = "Create booking",
            description = "Creates a new booking. Accessible by RENTER or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Booking created"),
        @ApiResponse(responseCode = "400", description = "Car already booked for selected dates"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
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

    @Operation(summary = "Get booking by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Booking found"),
        @ApiResponse(responseCode = "400", description = "Booking not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(bookingService.getBookingById(id)));
    }

    @Operation(
            summary = "Get all bookings",
            description = "Returns all bookings. Accessible by ADMINISTRATOR only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of bookings returned"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(
                bookingService.getAllBookings().stream().map(this::toResponse).toList());
    }

    @Operation(
            summary = "Get bookings by user ID",
            description =
                    "Returns all bookings for the specified user. Accessible by RENTER or ADMINISTRATOR.")
    @ApiResponse(responseCode = "200", description = "User bookings returned")
    @PreAuthorize("hasAnyRole('RENTER', 'ADMINISTRATOR')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(
                bookingService.getUserBookings(userId).stream().map(this::toResponse).toList());
    }

    @Operation(
            summary = "Cancel booking",
            description =
                    "Cancels the booking if within the cancellation deadline. Accessible by RENTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Booking cancelled"),
        @ApiResponse(responseCode = "400", description = "Cancellation not allowed"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('RENTER')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(bookingService.cancelBooking(id)));
    }

    @Operation(
            summary = "Change booking status",
            description = "Changes the status of a booking. Accessible by ADMINISTRATOR only.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Status changed"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{id}/status/change")
    public ResponseEntity<Void> changeBookingStatus(
            @PathVariable Long id, @RequestBody BookingStatusChange request) {
        bookingService.changeStatus(id, request.newStatus());

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete booking",
            description = "Deletes a finished booking. Accessible by ADMINISTRATOR only.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Booking deleted"),
        @ApiResponse(responseCode = "400", description = "Cannot delete active booking"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get bookings by owner",
            description =
                    "Returns all bookings for cars owned by the specified owner. Accessible by OWNER or ADMINISTRATOR.")
    @ApiResponse(responseCode = "200", description = "Owner bookings returned")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/owners/{ownerId}/bookings")
    public ResponseEntity<List<BookingForOwner>> getBookingsByOwner(@PathVariable Long ownerId) {

        List<BookingForOwner> responses =
                bookingService.getBookingsByOwnerId(ownerId).stream()
                        .map(this::toResponseForOwner)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Count bookings by owner")
    @ApiResponse(responseCode = "200", description = "Count returned")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/analytics/owners/{ownerId}/bookings")
    public ResponseEntity<Long> countBookingsByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(bookingService.countBookingsByOwnerId(ownerId));
    }

    @Operation(summary = "Count completed bookings by owner")
    @ApiResponse(responseCode = "200", description = "Count returned")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/analytics/owners/{ownerId}/bookings/whole")
    public ResponseEntity<Long> countCompletedBookingsByOwnerId(
            @PathVariable Long ownerId, @RequestParam BookingStatus status) {
        return ResponseEntity.ok(bookingService.countCompletedBookingsByOwnerId(ownerId, status));
    }

    @Operation(summary = "Get total revenue by owner")
    @ApiResponse(responseCode = "200", description = "Total revenue returned")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/analytics/owners/{ownerId}/revenue")
    public ResponseEntity<BigDecimal> sumTotalPriceByOwnerIdAndStatus(
            @PathVariable Long ownerId, @RequestParam BookingStatus status) {
        return ResponseEntity.ok(bookingService.sumTotalPriceByOwnerIdAndStatus(ownerId, status));
    }

    @Operation(summary = "Get monthly revenue by owner")
    @ApiResponse(responseCode = "200", description = "Monthly revenue data returned")
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

    @Operation(summary = "Get weekly load by owner")
    @ApiResponse(responseCode = "200", description = "Weekly load data returned")
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

    @Operation(
            summary = "Invite additional driver",
            description =
                    "Sends an invitation to add an additional driver to the booking. Accessible by RENTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Invitation created"),
        @ApiResponse(responseCode = "400", description = "User not found or max drivers reached"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
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

    @Operation(
            summary = "Accept driver invitation",
            description = "Accepts a pending driver invitation. Accessible by RENTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Invitation accepted"),
        @ApiResponse(
                responseCode = "400",
                description = "Invitation already processed or not found")
    })
    @PreAuthorize("hasAnyRole('RENTER')")
    @PostMapping("/drivers/{invitationId}/accept")
    public ResponseEntity<BookingDriverResponse> acceptInvitation(@PathVariable Long invitationId) {

        return ResponseEntity.ok(
                bookingDriverToResponse(bookingDriverService.acceptInvitation(invitationId)));
    }

    @Operation(
            summary = "Decline driver invitation",
            description = "Declines a pending driver invitation. Accessible by RENTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Invitation declined"),
        @ApiResponse(
                responseCode = "400",
                description = "Invitation already processed or not found")
    })
    @PreAuthorize("hasAnyRole('RENTER')")
    @PostMapping("/drivers/{invitationId}/decline")
    public ResponseEntity<BookingDriverResponse> declineInvitation(
            @PathVariable Long invitationId) {

        return ResponseEntity.ok(
                bookingDriverToResponse(bookingDriverService.declineInvitation(invitationId)));
    }

    @Operation(summary = "Get driver invitations by user ID")
    @ApiResponse(responseCode = "200", description = "Invitations returned")
    @GetMapping("/drivers/{userId}")
    public ResponseEntity<List<BookingDriverResponse>> getInvitationsByUserId(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                bookingDriverService.getByUserId(userId).stream()
                        .map(bookingDriver -> bookingDriverToResponse(bookingDriver))
                        .toList());
    }

    @Operation(summary = "Get all driver invitations")
    @ApiResponse(responseCode = "200", description = "All invitations returned")
    @GetMapping("/drivers")
    public ResponseEntity<List<BookingDriverResponse>> getAllInvitations() {
        return ResponseEntity.ok(
                bookingDriverService.getAll().stream()
                        .map(bookingDriver -> bookingDriverToResponse(bookingDriver))
                        .toList());
    }

    @Operation(
            summary = "Get active drivers for booking",
            description = "Returns accepted/pending drivers for a specific booking.")
    @ApiResponse(responseCode = "200", description = "Active drivers returned")
    @PreAuthorize("hasAnyRole('RENTER', 'OWNER', 'ADMINISTRATOR')")
    @GetMapping("/drivers/{bookingId}/active")
    public ResponseEntity<List<BookingDriverResponse>> getActiveDriversForBooking(
            @PathVariable Long bookingId) {

        List<BookingDriverResponse> responses =
                bookingDriverService.getActiveDriversByBookingId(bookingId).stream()
                        .map(this::bookingDriverToResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Count bookings by statuses (admin)")
    @ApiResponse(responseCode = "200", description = "Count returned")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/analytics/admin/bookings/count")
    public ResponseEntity<Long> countBookingsByStatuses(
            @RequestParam List<BookingStatus> statuses) {
        return ResponseEntity.ok(bookingService.countBookingsByStatuses(statuses));
    }

    @Operation(summary = "Get revenue for period (admin)")
    @ApiResponse(responseCode = "200", description = "Revenue returned")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/analytics/admin/revenue/period")
    public ResponseEntity<BigDecimal> sumLastMonthRevenue(
            @RequestParam BookingStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate) {
        return ResponseEntity.ok(bookingService.sumLastMonthRevenue(status, startDate));
    }

    @Operation(summary = "Count upcoming bookings (admin)")
    @ApiResponse(responseCode = "200", description = "Count returned")
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

    @Operation(summary = "Get monthly revenue (admin)")
    @ApiResponse(responseCode = "200", description = "Monthly revenue data returned")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/analytics/admin/revenue/monthly")
    public ResponseEntity<List<Object[]>> findMonthlyRevenue(
            @RequestParam BookingStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate) {
        return ResponseEntity.ok(bookingService.findMonthlyRevenue(status, startDate));
    }

    @Operation(summary = "Get bookings by day of week (admin)")
    @ApiResponse(responseCode = "200", description = "Load data returned")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/analytics/admin/load/day-of-week")
    public ResponseEntity<List<Object[]>> countBookingsByDayOfWeek(
            @RequestParam List<BookingStatus> activeStatuses) {
        return ResponseEntity.ok(bookingService.countBookingsByDayOfWeek(activeStatuses));
    }

    @Operation(
            summary = "Get car availability",
            description = "Returns active bookings for a car from today onwards.")
    @ApiResponse(responseCode = "200", description = "Car booking dates returned")
    @PreAuthorize("hasAnyRole('RENTER', 'OWNER', 'ADMINISTRATOR')")
    @GetMapping("/car/{carId}")
    public ResponseEntity<List<CarAvailabilityResponse>> getCarBookings(@PathVariable Long carId) {
        LocalDateTime now = LocalDateTime.now();

        List<CarAvailabilityResponse> responses =
                bookingService.getActiveBookingsByCarIdFromToday(carId).stream()
                        .map(this::toCarAvailabilityResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    private CarAvailabilityResponse toCarAvailabilityResponse(Booking booking) {
        return new CarAvailabilityResponse(booking.getStartDate(), booking.getEndDate());
    }

    @Operation(
            summary = "Get available car IDs",
            description = "Returns IDs of cars with no bookings in the given date range.")
    @ApiResponse(responseCode = "200", description = "Available car IDs returned")
    @PreAuthorize("hasAnyRole('RENTER', 'ADMINISTRATOR')")
    @GetMapping("/cars/available")
    public ResponseEntity<List<Long>> getAvailableCars(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate) {

        List<Long> availableCarIds = bookingService.getAvailableCarIds(startDate, endDate);

        return ResponseEntity.ok(availableCarIds);
    }
}
