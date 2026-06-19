package org.example.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class BookingTest {

  private Booking booking;

  @BeforeEach
  void setUp() {
    booking = new Booking();
    booking.setUserId(1L);
    booking.setCarId(2L);
    booking.setStartDate(LocalDateTime.now().plusDays(5));
    booking.setEndDate(LocalDateTime.now().plusDays(10));
    booking.setStatus(BookingStatus.CREATED);
    booking.setCancelDeadline(LocalDateTime.now().plusDays(3));
    booking.setTotalPrice(BigDecimal.valueOf(500));
  }

  @Test
  void cancel_fromCreated_beforeDeadline_shouldBecomeCancelled() {
    booking.setCancelDeadline(LocalDateTime.now().plusDays(1));
    booking.cancel();
    assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
  }

  @Test
  void cancel_afterDeadline_shouldThrow() {
    booking.setCancelDeadline(LocalDateTime.now().minusDays(1));
    assertThatThrownBy(booking::cancel)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Cancellation deadline has expired");
  }

  @Test
  void cancel_alreadyCancelled_shouldThrow() {
    booking.setStatus(BookingStatus.CANCELLED);
    booking.setCancelDeadline(LocalDateTime.now().plusDays(1));
    assertThatThrownBy(booking::cancel)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("already cancelled");
  }

  @Test
  void cancel_completed_shouldThrow() {
    booking.setStatus(BookingStatus.COMPLETED);
    booking.setCancelDeadline(LocalDateTime.now().plusDays(1));
    assertThatThrownBy(booking::cancel)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Cannot cancel completed booking");
  }

  @Test
  void calculateTotalPrice_3days_shouldReturn3xPrice() {
    booking.setStartDate(LocalDateTime.now().plusDays(1));
    booking.setEndDate(LocalDateTime.now().plusDays(4));
    booking.calculateTotalPrice(BigDecimal.valueOf(100));
    assertThat(booking.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(300));
  }

  @Test
  void calculateTotalPrice_sameDates_shouldThrow() {
    LocalDateTime same = LocalDateTime.now().plusDays(1);
    booking.setStartDate(same);
    booking.setEndDate(same);
    assertThatThrownBy(() -> booking.calculateTotalPrice(BigDecimal.valueOf(100)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("at least 1 day");
  }

  @Test
  void isActive_confirmedBooking_shouldReturnTrue() {
    booking.setStatus(BookingStatus.CONFIRMED);
    assertThat(booking.isActive()).isTrue();
  }

  @Test
  void isActive_pendingBooking_shouldReturnFalse() {
    booking.setStatus(BookingStatus.PENDING);
    assertThat(booking.isActive()).isFalse();
  }

  @Test
  void isPending_pendingBooking_shouldReturnTrue() {
    booking.setStatus(BookingStatus.PENDING);
    assertThat(booking.isPending()).isTrue();
  }

  @Test
  void isFinished_cancelledBooking_shouldReturnTrue() {
    booking.setStatus(BookingStatus.CANCELLED);
    assertThat(booking.isFinished()).isTrue();
  }

  @Test
  void isFinished_completedBooking_shouldReturnTrue() {
    booking.setStatus(BookingStatus.COMPLETED);
    assertThat(booking.isFinished()).isTrue();
  }

  @Test
  void isFinished_confirmedBooking_shouldReturnFalse() {
    booking.setStatus(BookingStatus.CONFIRMED);
    assertThat(booking.isFinished()).isFalse();
  }
}
