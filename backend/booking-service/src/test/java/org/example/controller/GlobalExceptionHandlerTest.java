package org.example.controller;

import org.example.application.BookingApplicationService;
import org.example.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
class GlobalExceptionHandlerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookingApplicationService bookingService;

  @Test
  void illegalArgumentException_shouldReturn400() throws Exception {
    when(bookingService.getBookingById(999L))
      .thenThrow(new IllegalArgumentException("Booking not found id=999"));

    mockMvc.perform(get("/booking/v1/999"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.error").value("Bad Request"))
      .andExpect(jsonPath("$.message").value("Booking not found id=999"))
      .andExpect(jsonPath("$.path").value("/booking/v1/999"));
  }

  @Test
  void illegalStateException_shouldReturn400() throws Exception {
    doThrow(new IllegalStateException("Cannot delete active booking"))
      .when(bookingService).deleteBooking(1L);

    mockMvc.perform(delete("/booking/v1/1"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").value("Cannot delete active booking"));
  }

  @Test
  void validationError_missingUserId_shouldReturn400WithDetails() throws Exception {
    String request = """
            {"carId": 2, "startDate": "2026-07-01T10:00:00", "endDate": "2026-07-10T10:00:00", "pricePerDay": 100}
            """;

    mockMvc.perform(post("/booking/v1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void validationError_missingCarId_shouldReturn400() throws Exception {
    String request = """
            {"userId": 1, "startDate": "2026-07-01T10:00:00", "endDate": "2026-07-10T10:00:00", "pricePerDay": 100}
            """;

    mockMvc.perform(post("/booking/v1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void errorResponse_shouldContainTimestamp() throws Exception {
    when(bookingService.getBookingById(999L))
      .thenThrow(new IllegalArgumentException("Booking not found id=999"));

    mockMvc.perform(get("/booking/v1/999"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void errorResponse_shouldContainPath() throws Exception {
    when(bookingService.getBookingById(1L))
      .thenThrow(new IllegalArgumentException("Booking not found"));

    mockMvc.perform(get("/booking/v1/1"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.path").value("/booking/v1/1"));
  }
}
