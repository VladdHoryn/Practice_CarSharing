package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.application.BookingApplicationService;
import org.example.config.SecurityConfig;
import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
class BookingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookingApplicationService bookingService;

  private ObjectMapper objectMapper;
  private Booking booking;
  private final LocalDateTime start = LocalDateTime.now().plusDays(5);
  private final LocalDateTime end = LocalDateTime.now().plusDays(10);

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
  void createBooking_validRequest_shouldReturn201() throws Exception {
    when(bookingService.createBooking(any(), any(), any(), any(), any()))
      .thenReturn(booking);

    String request = objectMapper.writeValueAsString(Map.of(
      "userId", 1,
      "carId", 2,
      "startDate", start.toString(),
      "endDate", end.toString(),
      "pricePerDay", 100
    ));

    mockMvc.perform(post("/booking/v1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.userId").value(1))
      .andExpect(jsonPath("$.carId").value(2))
      .andExpect(jsonPath("$.status").value("CREATED"));
  }

  @Test
  void createBooking_missingUserId_shouldReturn400() throws Exception {
    String request = objectMapper.writeValueAsString(Map.of(
      "carId", 2,
      "startDate", start.toString(),
      "endDate", end.toString(),
      "pricePerDay", 100
    ));

    mockMvc.perform(post("/booking/v1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }

  @Test
  void getBookingById_exists_shouldReturn200() throws Exception {
    when(bookingService.getBookingById(1L)).thenReturn(booking);

    mockMvc.perform(get("/booking/v1/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.status").value("CREATED"));
  }

  @Test
  void getBookingById_notFound_shouldReturn400() throws Exception {
    when(bookingService.getBookingById(999L))
      .thenThrow(new IllegalArgumentException("Booking not found id=999"));

    mockMvc.perform(get("/booking/v1/999"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value("Booking not found id=999"));
  }

  @Test
  void getAllBookings_shouldReturn200WithList() throws Exception {
    when(bookingService.getAllBookings()).thenReturn(List.of(booking));

    mockMvc.perform(get("/booking/v1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void getUserBookings_shouldReturn200() throws Exception {
    when(bookingService.getUserBookings(1L)).thenReturn(List.of(booking));

    mockMvc.perform(get("/booking/v1/user/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void submitBooking_shouldReturn200WithPendingStatus() throws Exception {
    booking.setStatus(BookingStatus.PENDING);
    when(bookingService.submitBooking(1L)).thenReturn(booking);

    mockMvc.perform(post("/booking/v1/1/submit"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  void confirmBooking_shouldReturn200WithConfirmedStatus() throws Exception {
    booking.setStatus(BookingStatus.CONFIRMED);
    when(bookingService.confirmBooking(1L)).thenReturn(booking);

    mockMvc.perform(post("/booking/v1/1/confirm"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("CONFIRMED"));
  }

  @Test
  void cancelBooking_shouldReturn200WithCancelledStatus() throws Exception {
    booking.setStatus(BookingStatus.CANCELLED);
    when(bookingService.cancelBooking(1L)).thenReturn(booking);

    mockMvc.perform(post("/booking/v1/1/cancel"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("CANCELLED"));
  }

  @Test
  void completeBooking_shouldReturn200WithCompletedStatus() throws Exception {
    booking.setStatus(BookingStatus.COMPLETED);
    when(bookingService.completeBooking(1L)).thenReturn(booking);

    mockMvc.perform(post("/booking/v1/1/complete"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  void deleteBooking_finished_shouldReturn204() throws Exception {
    doNothing().when(bookingService).deleteBooking(1L);

    mockMvc.perform(delete("/booking/v1/1"))
      .andExpect(status().isNoContent());
  }

  @Test
  void deleteBooking_active_shouldReturn400() throws Exception {
    doThrow(new IllegalStateException("Cannot delete active booking"))
      .when(bookingService).deleteBooking(1L);

    mockMvc.perform(delete("/booking/v1/1"))
      .andExpect(status().isBadRequest());
  }
}
