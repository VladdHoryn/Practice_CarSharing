package org.example.config;

import org.example.application.BookingApplicationService;
import org.example.controller.BookingController;
import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookingApplicationService bookingService;

  private Booking createBooking() {
    Booking booking = new Booking();
    booking.setId(1L);
    booking.setUserId(1L);
    booking.setCarId(2L);
    booking.setStartDate(LocalDateTime.now().plusDays(5));
    booking.setEndDate(LocalDateTime.now().plusDays(10));
    booking.setStatus(BookingStatus.CREATED);
    booking.setTotalPrice(BigDecimal.valueOf(500));
    booking.setCancelDeadline(LocalDateTime.now().plusDays(3));
    booking.setCreatedAt(LocalDateTime.now());
    booking.setUpdatedAt(LocalDateTime.now());
    return booking;
  }

  @Test
  void getEndpoint_withoutAuth_shouldReturn200() throws Exception {
    when(bookingService.getAllBookings()).thenReturn(List.of(createBooking()));

    mockMvc.perform(get("/booking/v1"))
      .andExpect(status().isOk());
  }

  @Test
  void getByIdEndpoint_withoutAuth_shouldReturn200() throws Exception {
    when(bookingService.getBookingById(1L)).thenReturn(createBooking());

    mockMvc.perform(get("/booking/v1/1"))
      .andExpect(status().isOk());
  }

  @Test
  void getUserEndpoint_withoutAuth_shouldReturn200() throws Exception {
    when(bookingService.getUserBookings(1L)).thenReturn(List.of(createBooking()));

    mockMvc.perform(get("/booking/v1/user/1"))
      .andExpect(status().isOk());
  }

  @Test
  void submitEndpoint_withoutAuth_shouldNotReturn401() throws Exception {
    Booking booking = createBooking();
    booking.setStatus(BookingStatus.PENDING);
    when(bookingService.submitBooking(1L)).thenReturn(booking);

    mockMvc.perform(post("/booking/v1/1/submit"))
      .andExpect(status().isOk());
  }

  @Test
  void confirmEndpoint_withoutAuth_shouldNotReturn401() throws Exception {
    Booking booking = createBooking();
    booking.setStatus(BookingStatus.CONFIRMED);
    when(bookingService.confirmBooking(1L)).thenReturn(booking);

    mockMvc.perform(post("/booking/v1/1/confirm"))
      .andExpect(status().isOk());
  }

  @Test
  void cancelEndpoint_withoutAuth_shouldNotReturn401() throws Exception {
    Booking booking = createBooking();
    booking.setStatus(BookingStatus.CANCELLED);
    when(bookingService.cancelBooking(1L)).thenReturn(booking);

    mockMvc.perform(post("/booking/v1/1/cancel"))
      .andExpect(status().isOk());
  }

  @Test
  void deleteEndpoint_withoutAuth_shouldNotReturn401() throws Exception {
    doNothing().when(bookingService).deleteBooking(1L);

    mockMvc.perform(delete("/booking/v1/1"))
      .andExpect(status().isNoContent());
  }
}
