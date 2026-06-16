package org.example.repository;

import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class BookingRepositoryTest {

  @Autowired
  private BookingRepository bookingRepository;

  private Booking savedBooking;

  @BeforeEach
  void setUp() {
    bookingRepository.deleteAll();
    savedBooking = bookingRepository.save(createBooking(
      1L, 10L,
      LocalDateTime.now().plusDays(5),
      LocalDateTime.now().plusDays(10),
      BookingStatus.CONFIRMED
    ));
  }

  private Booking createBooking(Long userId, Long carId,
                                LocalDateTime start, LocalDateTime end,
                                BookingStatus status) {
    Booking b = new Booking();
    b.setUserId(userId);
    b.setCarId(carId);
    b.setStartDate(start);
    b.setEndDate(end);
    b.setStatus(status);
    b.setTotalPrice(BigDecimal.valueOf(500));
    b.setCancelDeadline(start.minusDays(2));
    b.setCreatedAt(LocalDateTime.now());
    b.setUpdatedAt(LocalDateTime.now());
    return b;
  }

  @Test
  void save_shouldPersistBooking() {
    assertThat(savedBooking.getId()).isNotNull();
  }

  @Test
  void findById_shouldReturnBooking() {
    var found = bookingRepository.findById(savedBooking.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getUserId()).isEqualTo(1L);
  }

  @Test
  void findByUserId_shouldReturnUserBookings() {
    bookingRepository.save(createBooking(1L, 20L,
      LocalDateTime.now().plusDays(15),
      LocalDateTime.now().plusDays(20),
      BookingStatus.CREATED));

    List<Booking> result = bookingRepository.findByUserId(1L);
    assertThat(result).hasSize(2);
  }

  @Test
  void findByUserId_differentUser_shouldReturnEmpty() {
    List<Booking> result = bookingRepository.findByUserId(999L);
    assertThat(result).isEmpty();
  }

  @Test
  void isCarAlreadyBooked_overlappingDates_shouldReturnTrue() {
    boolean result = bookingRepository.isCarAlreadyBooked(
      10L,
      LocalDateTime.now().plusDays(6),
      LocalDateTime.now().plusDays(8)
    );
    assertThat(result).isTrue();
  }

  @Test
  void isCarAlreadyBooked_nonOverlappingDates_shouldReturnFalse() {
    boolean result = bookingRepository.isCarAlreadyBooked(
      10L,
      LocalDateTime.now().plusDays(15),
      LocalDateTime.now().plusDays(20)
    );
    assertThat(result).isFalse();
  }

  @Test
  void isCarAlreadyBooked_cancelledBooking_shouldReturnFalse() {
    bookingRepository.save(createBooking(2L, 30L,
      LocalDateTime.now().plusDays(5),
      LocalDateTime.now().plusDays(10),
      BookingStatus.CANCELLED));

    boolean result = bookingRepository.isCarAlreadyBooked(
      30L,
      LocalDateTime.now().plusDays(6),
      LocalDateTime.now().plusDays(8)
    );
    assertThat(result).isFalse();
  }

  @Test
  void isCarAlreadyBooked_differentCar_shouldReturnFalse() {
    boolean result = bookingRepository.isCarAlreadyBooked(
      999L,
      LocalDateTime.now().plusDays(6),
      LocalDateTime.now().plusDays(8)
    );
    assertThat(result).isFalse();
  }

  @Test
  void delete_shouldRemoveBooking() {
    bookingRepository.delete(savedBooking);
    assertThat(bookingRepository.findById(savedBooking.getId())).isEmpty();
  }

  @Test
  void findAll_shouldReturnAllBookings() {
    bookingRepository.save(createBooking(2L, 20L,
      LocalDateTime.now().plusDays(1),
      LocalDateTime.now().plusDays(3),
      BookingStatus.CREATED));

    assertThat(bookingRepository.findAll()).hasSize(2);
  }
}
