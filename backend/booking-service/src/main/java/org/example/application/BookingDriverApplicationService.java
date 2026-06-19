package org.example.application;

import java.util.List;

import org.example.domain.BookingDriver;
import org.example.domain.BookingDriverStatus;
import org.example.repository.BookingDriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingDriverApplicationService {

    private static final int MAX_ADDITIONAL_DRIVERS = 2;

    private final BookingDriverRepository bookingDriverRepository;

    public BookingDriver createInvitation(
            Long bookingId, Long userId, String email, String driverCode) {

        if (bookingDriverRepository.existsByBookingIdAndUserId(bookingId, userId)) {
            throw new IllegalStateException("User has already been invited to this booking");
        }

        long currentDrivers =
                bookingDriverRepository.countByBookingIdAndStatusIn(
                        bookingId,
                        List.of(BookingDriverStatus.PENDING, BookingDriverStatus.ACCEPTED));

        if (currentDrivers >= MAX_ADDITIONAL_DRIVERS) {
            throw new IllegalStateException("Maximum number of additional drivers reached");
        }

        BookingDriver bookingDriver = new BookingDriver();

        bookingDriver.setBookingId(bookingId);
        bookingDriver.setUserId(userId);
        bookingDriver.setEmail(email);
        bookingDriver.setDriverCode(driverCode);
        bookingDriver.setStatus(BookingDriverStatus.PENDING);

        return bookingDriverRepository.save(bookingDriver);
    }

    public BookingDriver acceptInvitation(Long invitationId) {

        BookingDriver bookingDriver =
                bookingDriverRepository
                        .findById(invitationId)
                        .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (!bookingDriver.isPending()) {
            throw new IllegalStateException("Invitation has already been processed");
        }

        bookingDriver.accept();

        return bookingDriverRepository.save(bookingDriver);
    }

    public BookingDriver declineInvitation(Long invitationId) {

        BookingDriver bookingDriver =
                bookingDriverRepository
                        .findById(invitationId)
                        .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (!bookingDriver.isPending()) {
            throw new IllegalStateException("Invitation has already been processed");
        }

        bookingDriver.decline();

        return bookingDriverRepository.save(bookingDriver);
    }

    @Transactional(readOnly = true)
    public BookingDriver getById(Long id) {
        return bookingDriverRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
    }

    @Transactional(readOnly = true)
    public List<BookingDriver> getByUserId(Long userId) {
        return bookingDriverRepository
                .findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
    }

    @Transactional(readOnly = true)
    public List<BookingDriver> getAll() {
        return bookingDriverRepository.findAll();
    }
}
