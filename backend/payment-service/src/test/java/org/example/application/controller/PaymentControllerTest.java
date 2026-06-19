package org.example.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.example.application.PaymentApplicationService;
import org.example.controller.PaymentController;
import org.example.domain.Payment;
import org.example.domain.PaymentMethod;
import org.example.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PaymentApplicationService paymentService;

  private ObjectMapper objectMapper;
  private Payment payment;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    payment = new Payment();
    payment.setId(1L);
    payment.setBookingId(1L);
    payment.setAmount(BigDecimal.valueOf(500));
    payment.setMethod(PaymentMethod.CARD);
    payment.setCurrency("UAH");
    payment.setStatus(PaymentStatus.CREATED);
    payment.setPaymentDate(LocalDateTime.now());
    payment.setIdempotencyKey("test-key");
    payment.setCreatedAt(LocalDateTime.now());
    payment.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void createPayment_validRequest_shouldReturn201() throws Exception {
    when(paymentService.createPayment(any(), any(), any(), any())).thenReturn(payment);

    String request = objectMapper.writeValueAsString(Map.of(
      "bookingId", 1,
      "amount", 500,
      "method", "CARD",
      "currency", "UAH"
    ));

    mockMvc.perform(post("/payment/v1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.bookingId").value(1))
      .andExpect(jsonPath("$.status").value("CREATED"));
  }

  @Test
  void createPayment_missingBookingId_shouldReturn400() throws Exception {
    String request = objectMapper.writeValueAsString(Map.of(
      "amount", 500,
      "method", "CARD",
      "currency", "UAH"
    ));

    mockMvc.perform(post("/payment/v1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createPayment_missingCurrency_shouldReturn400() throws Exception {
    String request = objectMapper.writeValueAsString(Map.of(
      "bookingId", 1,
      "amount", 500,
      "method", "CARD"
    ));

    mockMvc.perform(post("/payment/v1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }

  @Test
  void getById_exists_shouldReturn200() throws Exception {
    when(paymentService.getById(1L)).thenReturn(payment);

    mockMvc.perform(get("/payment/v1/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.status").value("CREATED"));
  }

  @Test
  void getById_notFound_shouldReturn400() throws Exception {
    when(paymentService.getById(999L))
      .thenThrow(new EntityNotFoundException("Payment not found with id=999"));

    mockMvc.perform(get("/payment/v1/999"))
      .andExpect(status().isBadRequest());
  }

  @Test
  void getAll_shouldReturn200() throws Exception {
    when(paymentService.getAll()).thenReturn(List.of(payment));

    mockMvc.perform(get("/payment/v1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void updatePayment_shouldReturn200() throws Exception {
    payment.setAmount(BigDecimal.valueOf(1000));
    when(paymentService.updatePayment(eq(1L), any(), any(), any())).thenReturn(payment);

    String request = objectMapper.writeValueAsString(Map.of(
      "amount", 1000,
      "method", "GOOGLE_PAY",
      "currency", "USD"
    ));

    mockMvc.perform(put("/payment/v1/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.amount").value(1000));
  }

  @Test
  void deletePayment_shouldReturn204() throws Exception {
    doNothing().when(paymentService).deletePayment(1L);

    mockMvc.perform(delete("/payment/v1/1"))
      .andExpect(status().isNoContent());
  }

  @Test
  void refundPayment_shouldReturn200() throws Exception {
    payment.setStatus(PaymentStatus.REFUNDED);
    when(paymentService.refundPayment(1L)).thenReturn(payment);

    mockMvc.perform(patch("/payment/v1/1/refund"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("REFUNDED"));
  }
}
