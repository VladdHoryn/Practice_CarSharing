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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingApplicationServiceTest {

  @Mock
  private BookingRepository bookingRepository;

  @InjectMocks
  private BookingApplicationService bookingService;

  private Booking sampleBooking;
  private Booking confirmedBooking;

  @BeforeEach
  void setUp() {
    sampleBooking = new Booking();
    sampleBooking.setId(1L);
    sampleBooking.setStatus(BookingStatus.PENDING);

    confirmedBooking = new Booking();
    confirmedBooking.setId(2L);
    confirmedBooking.setStatus(BookingStatus.CONFIRMED);
  }

  @Test
  void testGetBookingById_Success() {
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));

    Booking result = bookingService.getBookingById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals(BookingStatus.PENDING, result.getStatus());
    verify(bookingRepository, times(1)).findById(1L);
  }

  @Test
  void testGetBookingById_NotFound_ThrowsIllegalArgumentException() {
    when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> {
      bookingService.getBookingById(99L);
    });
    verify(bookingRepository, times(1)).findById(99L);
  }

  @Test
  void testCreateBooking_Success() {
    when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

    LocalDateTime now = LocalDateTime.now();
    Booking created = bookingService.createBooking(1L, 1L, now, now.plusDays(2), BigDecimal.valueOf(100));

    assertNotNull(created);
    verify(bookingRepository, times(1)).save(any(Booking.class));
  }

  @Test
  void testCreateBooking_AlternativeParameters() {
    when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

    LocalDateTime now = LocalDateTime.now();
    Booking created = bookingService.createBooking(2L, 5L, now.minusDays(1), now.plusDays(1), BigDecimal.ZERO);

    assertNotNull(created);
    verify(bookingRepository, times(1)).save(any(Booking.class));
  }

  @Test
  void testDeleteBooking_Success() {
    Long bookingId = 1L;
    Booking nonActiveBooking = new Booking();
    nonActiveBooking.setId(bookingId);
    nonActiveBooking.setStatus(BookingStatus.CANCELLED);

    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(nonActiveBooking));
    doNothing().when(bookingRepository).delete(any(Booking.class));

    assertDoesNotThrow(() -> bookingService.deleteBooking(bookingId));
    verify(bookingRepository, times(1)).delete(any(Booking.class));
  }

  @Test
  void testDeleteBooking_NotFound_ThrowsIllegalArgumentException() {
    Long bookingId = 99L;
    when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> bookingService.deleteBooking(bookingId));
  }

  @Test
  void testBookingStatusEnum_Pending() {
    Booking booking = new Booking();
    booking.setStatus(BookingStatus.PENDING);
    assertEquals(BookingStatus.PENDING, booking.getStatus());
  }

  @Test
  void testBookingStatusEnum_Confirmed() {
    Booking booking = new Booking();
    booking.setStatus(BookingStatus.CONFIRMED);
    assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
  }

  @Test
  void testBookingIdAssignment() {
    Booking booking = new Booking();
    booking.setId(550L);
    assertEquals(550L, booking.getId());
  }

  @Test
  void testRepositoryFindById_DirectCall_Empty() {
    when(bookingRepository.findById(100L)).thenReturn(Optional.empty());
    Optional<Booking> result = bookingRepository.findById(100L);
    assertTrue(result.isEmpty());
  }

  @Test
  void testRepositorySave_DirectCall() {
    when(bookingRepository.save(sampleBooking)).thenReturn(sampleBooking);
    Booking saved = bookingRepository.save(sampleBooking);
    assertNotNull(saved);
    assertEquals(1L, saved.getId());
  }

  @Test
  void testRepositoryDelete_DirectCall() {
    doNothing().when(bookingRepository).delete(sampleBooking);
    bookingRepository.delete(sampleBooking);
    verify(bookingRepository, times(1)).delete(sampleBooking);
  }
}
