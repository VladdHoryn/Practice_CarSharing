package org.example.application.controller;

import org.example.application.PaymentApplicationService;
import org.example.controller.PaymentController;
import org.example.domain.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
public class PaymentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PaymentApplicationService paymentService;

  @Test
  @WithMockUser(roles = {"USER"})
  public void shouldReturnOk_WhenFetchingPayments() throws Exception {

    when(paymentService.getAll()).thenReturn(List.of(new Payment()));

    mockMvc.perform(get("/payment/v1"))
      .andExpect(status().isOk());
  }
}
