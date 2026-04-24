package org.example.application;

import org.example.domain.Booking;
import org.example.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingApplicationServiceTest {

  @Mock
  private BookingRepository bookingRepository;

  @InjectMocks
  private BookingApplicationService bookingApplicationService;

  @Test
  void shouldReturnAllBookings() {
    // Arrange
    Booking booking = new Booking();
    when(bookingRepository.findAll()).thenReturn(List.of(booking));

    // Act: Змінюємо тип на List<Booking>, бо сервіс повертає саме його
    List<Booking> result = bookingApplicationService.getAllBookings();

    // Assert
    assertNotNull(result);
    assertFalse(result.isEmpty());
    verify(bookingRepository, times(1)).findAll();
  }

  @Test
  void shouldReturnBookingById() {
    // Arrange
    Long bookingId = 1L;
    Booking booking = new Booking();
    booking.setId(bookingId);
    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

    // Act: Тут теж тип просто Booking
    Booking result = bookingApplicationService.getBookingById(bookingId);

    // Assert
    assertNotNull(result);
    assertEquals(bookingId, result.getId());
    verify(bookingRepository, times(1)).findById(bookingId);
  }
}
