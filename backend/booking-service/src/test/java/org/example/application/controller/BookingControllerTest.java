package org.example.application.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.example.application.BookingApplicationService;
import org.example.controller.BookingController;
import org.example.domain.Booking;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private BookingApplicationService bookingService;

    @Test
    @WithMockUser(roles = {"USER"})
    public void shouldReturnAllBookings() throws Exception {
        Booking booking = new Booking();
        when(bookingService.getAllBookings()).thenReturn(List.of(booking));

        mockMvc.perform(get("/booking/v1")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void shouldReturnBookingById() throws Exception {
        Long bookingId = 1L;
        Booking booking = new Booking();

        when(bookingService.getBookingById(anyLong())).thenReturn(booking);

        mockMvc.perform(get("/booking/v1/" + bookingId)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void shouldConfirmBooking() throws Exception {
        Long bookingId = 1L;
        Booking booking = new Booking();

        when(bookingService.confirmBooking(anyLong())).thenReturn(booking);

        mockMvc.perform(post("/booking/v1/" + bookingId + "/confirm").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void shouldReturnUserBookings() throws Exception {
        Long userId = 100L;
        when(bookingService.getUserBookings(anyLong())).thenReturn(List.of(new Booking()));

        mockMvc.perform(get("/booking/v1/user/" + userId)).andExpect(status().isOk());
    }
}
