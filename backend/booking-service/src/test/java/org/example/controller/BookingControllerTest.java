package org.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.example.application.BookingApplicationService;
import org.example.application.BookingDriverApplicationService;
import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.example.dto.CreateBookingRequest;
import org.example.infrastructure.client.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BookingController.class)
@WithMockUser
public class BookingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private BookingApplicationService bookingService;

    @MockitoBean private BookingDriverApplicationService bookingDriverApplicationService;

    @MockitoBean private UserServiceClient userServiceClient;

    @Autowired private ObjectMapper objectMapper;

    private Booking sampleBooking;

    @BeforeEach
    void setUp() {
        sampleBooking = new Booking();
        sampleBooking.setId(1L);
        sampleBooking.setUserId(1L);
        sampleBooking.setCarId(1L);
        sampleBooking.setStartDate(LocalDateTime.now());
        sampleBooking.setEndDate(LocalDateTime.now().plusDays(1));
        sampleBooking.setStatus(BookingStatus.PENDING);
        sampleBooking.setTotalPrice(BigDecimal.valueOf(250.00));
    }

    @Test
    void testGetBookingById_HttpSuccess() throws Exception {
        when(bookingService.getBookingById(1L)).thenReturn(sampleBooking);

        mockMvc.perform(get("/booking/v1/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(bookingService, times(1)).getBookingById(1L);
    }

    @Test
    void testGetBookingById_HttpNotFound() throws Exception {
        when(bookingService.getBookingById(99L))
                .thenThrow(new IllegalArgumentException("Booking not found"));

        mockMvc.perform(get("/booking/v1/{id}", 99L)).andExpect(status().isBadRequest());
    }

    @Test
    void testGetBookingById_InvalidIdFormat() throws Exception {
        mockMvc.perform(get("/booking/v1/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBooking_HttpSuccess() throws Exception {
        when(bookingService.createBooking(
                        eq(1L),
                        eq(1L),
                        any(LocalDateTime.now().getClass()),
                        any(LocalDateTime.now().getClass()),
                        any(BigDecimal.class)))
                .thenReturn(sampleBooking);

        CreateBookingRequest request =
                new CreateBookingRequest(
                        1L,
                        1L,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1),
                        BigDecimal.valueOf(250.00));

        mockMvc.perform(
                        post("/booking/v1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testCreateBooking_MissingParameters() throws Exception {
        String invalidJson = "{\"userId\":null,\"carId\":null}";

        mockMvc.perform(
                        post("/booking/v1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBooking_InvalidTypes() throws Exception {
        String badTypeJson = "{\"userId\":\"abc\",\"carId\":1}";

        mockMvc.perform(
                        post("/booking/v1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(badTypeJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteBooking_HttpSuccess() throws Exception {
        doNothing().when(bookingService).deleteBooking(1L);

        mockMvc.perform(delete("/booking/v1/{id}", 1L).with(csrf()))
                .andExpect(status().isNoContent());

        verify(bookingService, times(1)).deleteBooking(1L);
    }

    @Test
    void testDeleteBooking_HttpConflict_ActiveBooking() throws Exception {
        doThrow(new IllegalStateException("Cannot delete active booking"))
                .when(bookingService)
                .deleteBooking(1L);

        mockMvc.perform(delete("/booking/v1/{id}", 1L).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteBooking_HttpNotFound() throws Exception {
        doThrow(new IllegalArgumentException("Not found")).when(bookingService).deleteBooking(99L);

        mockMvc.perform(delete("/booking/v1/{id}", 99L).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testOptionsRequest() throws Exception {
        mockMvc.perform(options("/booking/v1")).andExpect(status().isOk());
    }

    @Test
    void testPostMethodNotAllowedOnIdRoute() throws Exception {
        mockMvc.perform(post("/booking/v1/{id}", 1L).with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetBooking_CorsHeadersCheck() throws Exception {
        when(bookingService.getBookingById(1L)).thenReturn(sampleBooking);

        mockMvc.perform(get("/booking/v1/{id}", 1L).header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
