package org.example.application;

import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.example.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingApplicationServiceTest {

  @Mock
  private BookingRepository bookingRepository;

  @InjectMocks
  private BookingApplicationService bookingService;

  private Booking booking;
  private final LocalDateTime start = LocalDateTime.now().plusDays(5);
  private final LocalDateTime end = LocalDateTime.now().plusDays(10);

  @BeforeEach
  void setUp() {
    booking = new Booking();
    booking.setId(1L);
    booking.setUserId(1L);
    booking.setCarId(2L);
    booking.setStartDate(start);
    booking.setEndDate(end);
    booking.setStatus(BookingStatus.CREATED);
    booking.setTotalPrice(BigDecimal.valueOf(500));
    booking.setCancelDeadline(start.minusDays(2));
    booking.setCreatedAt(LocalDateTime.now());
    booking.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void createBooking_carFree_shouldSaveAndReturn() {
    when(bookingRepository.isCarAlreadyBooked(any(), any(), any())).thenReturn(false);
    when(bookingRepository.save(any())).thenReturn(booking);

    Booking result = bookingService.createBooking(1L, 2L, start, end, BigDecimal.valueOf(100));

    assertThat(result).isNotNull();
    verify(bookingRepository).save(any(Booking.class));
  }

  @Test
  void createBooking_carAlreadyBooked_shouldThrow() {
    when(bookingRepository.isCarAlreadyBooked(any(), any(), any())).thenReturn(true);

    assertThatThrownBy(() ->
      bookingService.createBooking(1L, 2L, start, end, BigDecimal.valueOf(100)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("вже заброньовано");

    verify(bookingRepository, never()).save(any());
  }

  @Test
  void getBookingById_exists_shouldReturn() {
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    Booking result = bookingService.getBookingById(1L);

    assertThat(result.getId()).isEqualTo(1L);
  }

  @Test
  void getBookingById_notFound_shouldThrow() {
    when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookingService.getBookingById(999L))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Booking not found");
  }

  @Test
  void getAllBookings_shouldReturnAll() {
    when(bookingRepository.findAll()).thenReturn(List.of(booking));

    List<Booking> result = bookingService.getAllBookings();

    assertThat(result).hasSize(1);
  }

  @Test
  void getUserBookings_shouldReturnUserBookings() {
    when(bookingRepository.findByUserId(1L)).thenReturn(List.of(booking));

    List<Booking> result = bookingService.getUserBookings(1L);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getUserId()).isEqualTo(1L);
  }

  @Test
  void submitBooking_fromCreated_shouldReturnPending() {
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    Booking result = bookingService.submitBooking(1L);

    assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
  }

  @Test
  void submitBooking_notFound_shouldThrow() {
    when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookingService.submitBooking(99L))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void confirmBooking_fromPending_shouldReturnConfirmed() {
    booking.setStatus(BookingStatus.PENDING);
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    Booking result = bookingService.confirmBooking(1L);

    assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
  }

  @Test
  void confirmBooking_fromCreated_shouldThrow() {
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    assertThatThrownBy(() -> bookingService.confirmBooking(1L))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void cancelBooking_beforeDeadline_shouldReturnCancelled() {
    booking.setCancelDeadline(LocalDateTime.now().plusDays(1));
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    Booking result = bookingService.cancelBooking(1L);

    assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
  }

  @Test
  void cancelBooking_afterDeadline_shouldThrow() {
    booking.setCancelDeadline(LocalDateTime.now().minusDays(1));
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    assertThatThrownBy(() -> bookingService.cancelBooking(1L))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("deadline");
  }

  @Test
  void completeBooking_fromConfirmed_shouldReturnCompleted() {
    booking.setStatus(BookingStatus.CONFIRMED);
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    Booking result = bookingService.completeBooking(1L);

    assertThat(result.getStatus()).isEqualTo(BookingStatus.COMPLETED);
  }

  @Test
  void completeBooking_fromPending_shouldThrow() {
    booking.setStatus(BookingStatus.PENDING);
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    assertThatThrownBy(() -> bookingService.completeBooking(1L))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void deleteBooking_finishedBooking_shouldDelete() {
    booking.setStatus(BookingStatus.CANCELLED);
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    bookingService.deleteBooking(1L);

    verify(bookingRepository).delete(booking);
  }

  @Test
  void deleteBooking_activeBooking_shouldThrow() {
    booking.setStatus(BookingStatus.CONFIRMED);
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    assertThatThrownBy(() -> bookingService.deleteBooking(1L))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Cannot delete active booking");

    verify(bookingRepository, never()).delete(any());
  }
}
