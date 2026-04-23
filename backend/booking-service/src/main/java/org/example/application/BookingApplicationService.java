package org.example.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Booking;
import org.example.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingApplicationService {

  private final BookingRepository bookingRepository;

  @Transactional
  public Booking createBooking(Long userId, Long carId,
                               LocalDateTime start,
                               LocalDateTime end,
                               Float pricePerDay) {

    log.info("Creating booking: userId={}, carId={}", userId, carId);

    Booking booking = new Booking();
    booking.setUserId(userId);
    booking.setCarId(carId);
    booking.setStartDate(start);
    booking.setEndDate(end);

    booking.calculateTotalPrice(pricePerDay);

    Booking saved = bookingRepository.save(booking);

    return saved;
  }

  public Booking getBookingById(Long id) {
    return bookingRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Booking not found id=" + id));
  }

  public List<Booking> getAllBookings() {
    return bookingRepository.findAll();
  }

  public List<Booking> getUserBookings(Long userId) {
    return bookingRepository.findByUserId(userId);
  }

  // CREATED → PENDING
  @Transactional
  public Booking submitBooking(Long bookingId) {
    Booking booking = getBookingById(bookingId);

    booking.submitForProcessing();

    return booking;
  }

  // PENDING → CONFIRMED
  @Transactional
  public Booking confirmBooking(Long bookingId) {
    Booking booking = getBookingById(bookingId);

    booking.confirm();

    return booking;
  }

  // ANY → CANCELLED
  @Transactional
  public Booking cancelBooking(Long bookingId) {
    Booking booking = getBookingById(bookingId);

    booking.cancel();

    return booking;
  }

  // CONFIRMED → COMPLETED
  @Transactional
  public Booking completeBooking(Long bookingId) {
    Booking booking = getBookingById(bookingId);

    booking.complete();

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
}
