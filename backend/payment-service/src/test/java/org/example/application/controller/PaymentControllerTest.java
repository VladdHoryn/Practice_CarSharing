package org.example.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import org.example.application.PaymentApplicationService;
import org.example.config.TestSecurityConfig;
import org.example.controller.PaymentController;
import org.example.domain.Payment;
import org.example.domain.PaymentMethod;
import org.example.domain.PaymentStatus;
import org.example.dto.ChangePaymentStatus;
import org.example.dto.CreatePaymentRequest;
import org.example.dto.UpdatePaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PaymentController.class)
@Import(TestSecurityConfig.class)
@ImportAutoConfiguration({
    SecurityAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class
}) // Додано
class PaymentControllerTest {

    @MockitoBean private JwtDecoder jwtDecoder;

    @Autowired private MockMvc mockMvc;

    @MockitoBean private PaymentApplicationService paymentService;

    @Autowired private ObjectMapper objectMapper;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(1L);
        payment.setBookingId(10L);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setMethod(PaymentMethod.CARD);
        payment.setStatus(PaymentStatus.CREATED);
        payment.setCurrency("USD");
    }

    @Nested
    @DisplayName("GET /payment/v1/{id}")
    class GetById {

        @Test
        @WithMockUser
        @DisplayName("повертає 200 та платіж за ID")
        void shouldReturnPaymentById() throws Exception {
            when(paymentService.getById(1L)).thenReturn(payment);
            mockMvc.perform(get("/payment/v1/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @WithMockUser
        @DisplayName("повертає 5xx якщо платіж не знайдено")
        void shouldReturn5xxWhenNotFound() throws Exception {
            when(paymentService.getById(99L))
                    .thenThrow(new EntityNotFoundException("Payment not found"));
            mockMvc.perform(get("/payment/v1/99")).andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("повертає 4xx для неавторизованого")
        void shouldReturn4xxForAnonymous() throws Exception {
            mockMvc.perform(get("/payment/v1/1")).andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("GET /payment/v1")
    class GetAll {

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR отримує всі платежі")
        void shouldReturnAllPaymentsForAdmin() throws Exception {
            when(paymentService.getAll()).thenReturn(List.of(payment));
            mockMvc.perform(get("/payment/v1")).andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            mockMvc.perform(get("/payment/v1")).andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("звичайний USER отримує 403")
        void shouldReturn403ForUser() throws Exception {
            mockMvc.perform(get("/payment/v1")).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /payment/v1")
    class CreatePayment {

        private CreatePaymentRequest validRequest() {
            return new CreatePaymentRequest(
                    10L, BigDecimal.valueOf(500), PaymentMethod.CARD, "USD");
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER може створити платіж")
        void shouldCreatePaymentForRenter() throws Exception {
            when(paymentService.createPayment(any(), any(), any(), any())).thenReturn(payment);
            mockMvc.perform(
                            post("/payment/v1")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR може створити платіж")
        void shouldCreatePaymentForAdmin() throws Exception {
            when(paymentService.createPayment(any(), any(), any(), any())).thenReturn(payment);
            mockMvc.perform(
                            post("/payment/v1")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser
        @DisplayName("звичайний USER отримує 403")
        void shouldReturn403ForUser() throws Exception {
            mockMvc.perform(
                            post("/payment/v1")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("повертає 400 для некоректного запиту")
        void shouldReturn400ForInvalidRequest() throws Exception {
            String invalidJson = "{\"bookingId\":null,\"amount\":null}";
            mockMvc.perform(
                            post("/payment/v1")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /payment/v1/{id}")
    class UpdatePayment {

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR може оновити платіж")
        void shouldUpdatePaymentForAdmin() throws Exception {
            when(paymentService.updatePayment(eq(1L), any(), any(), any())).thenReturn(payment);
            UpdatePaymentRequest req =
                    new UpdatePaymentRequest(
                            BigDecimal.valueOf(600), PaymentMethod.GOOGLE_PAY, "EUR");
            mockMvc.perform(
                            put("/payment/v1/1")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            UpdatePaymentRequest req =
                    new UpdatePaymentRequest(BigDecimal.valueOf(600), null, null);
            mockMvc.perform(
                            put("/payment/v1/1")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /payment/v1/{id}")
    class DeletePayment {

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR може видалити платіж")
        void shouldDeletePaymentForAdmin() throws Exception {
            doNothing().when(paymentService).deletePayment(1L);
            mockMvc.perform(delete("/payment/v1/1").with(csrf())).andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            mockMvc.perform(delete("/payment/v1/1").with(csrf())).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /payment/v1/{id}/refund")
    class RefundPayment {

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER може ініціювати повернення")
        void shouldRefundForRenter() throws Exception {
            payment.setStatus(PaymentStatus.REFUNDED);
            when(paymentService.refundPayment(1L)).thenReturn(payment);
            mockMvc.perform(patch("/payment/v1/1/refund").with(csrf())).andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR може ініціювати повернення")
        void shouldRefundForAdmin() throws Exception {
            payment.setStatus(PaymentStatus.REFUNDED);
            when(paymentService.refundPayment(1L)).thenReturn(payment);
            mockMvc.perform(patch("/payment/v1/1/refund").with(csrf())).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /payment/v1/{id}/status/change")
    class ChangeStatus {

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR може змінити статус")
        void shouldChangeStatusForAdmin() throws Exception {
            doNothing().when(paymentService).changeStatus(eq(1L), any());
            ChangePaymentStatus req = new ChangePaymentStatus(PaymentStatus.SUCCESS);
            mockMvc.perform(
                            post("/payment/v1/1/status/change")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            ChangePaymentStatus req = new ChangePaymentStatus(PaymentStatus.SUCCESS);
            mockMvc.perform(
                            post("/payment/v1/1/status/change")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }
    }
}
