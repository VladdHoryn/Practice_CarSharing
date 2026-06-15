package org.example.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.transaction.Transactional;

import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.example.repository.BookingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingApplicationService {

    private final BookingRepository bookingRepository;

    @Transactional
    public Booking createBooking(
            Long userId,
            Long carId,
            LocalDateTime start,
            LocalDateTime end,
            BigDecimal pricePerDay) {

        log.info("Creating booking: userId={}, carId={}", userId, carId);

        if (bookingRepository.isCarAlreadyBooked(carId, start, end)) {
            log.warn("Car {} is already booked for dates {} - {}", carId, start, end);
            throw new IllegalArgumentException(
                    "Цей автомобіль вже заброньовано на обрані дати. Будь ласка, оберіть інші дні.");
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setCarId(carId);
        booking.setStartDate(start);
        booking.setEndDate(end);

        booking.setStatus(BookingStatus.CREATED);

        booking.calculateTotalPrice(pricePerDay);

        booking.setCreatedAt(LocalDateTime.now());

        // Встановлюємо дедлайн скасування за 2 дні до початку
        booking.setCancelDeadline(start.minusDays(2));

        Booking saved = bookingRepository.save(booking);

        return saved;
    }

    public Booking getBookingById(Long id) {
        return bookingRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found id=" + id));
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Transactional
    public void changeStatus(Long bookingId, BookingStatus newStatus) {
        Booking booking = getBookingById(bookingId);

        booking.changeStatus(newStatus);
    }

    // ANY → CANCELLED
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        booking.cancel();

        return booking;
    }

    @Transactional
    public void deleteBooking(Long id) {
        log.info("Deleting booking id={}", id);

        Booking booking = getBookingById(id);

        if (!booking.isFinished()) {
            throw new IllegalStateException("Cannot delete active booking");
        }

        bookingRepository.delete(booking);
    }

    // OWNER ANALYTICS

    public long countBookingsByOwnerId(Long ownerId){
      return bookingRepository.countBookingsByOwnerId(ownerId);
    }

    public long countCompletedBookingsByOwnerId(Long ownerId, BookingStatus status){
      return bookingRepository.countCompletedBookingsByOwnerId(ownerId, status);
    }

    public double sumTotalPriceByOwnerIdAndStatus(Long ownerId, BookingStatus status){
      return bookingRepository.sumTotalPriceByOwnerIdAndStatus(ownerId, status);
    }

    public List<Object[]> findMonthlyRevenueByOwnerId(Long ownerId, BookingStatus status, LocalDateTime startDate){
      return bookingRepository.findMonthlyRevenueByOwnerId(ownerId, status, startDate);
    }

    public List<Object[]> countBookedCarsByDayForOwner(Long ownerId, List<BookingStatus> activeStatuses,
                                                       LocalDateTime startDate, LocalDateTime endDate){
      return bookingRepository.countBookedCarsByDayForOwner(ownerId, activeStatuses, startDate, endDate);
    }
}
