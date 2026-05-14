package org.example.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.example.domain.Payment;
import org.example.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PaymentApplicationServiceTest {

  @Mock
  private PaymentRepository paymentRepository;

  @InjectMocks
  private PaymentApplicationService paymentService;

  @Test
  void shouldReturnPaymentById() {
    Long id = 1L;
    Payment payment = new Payment();

    when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));

    Payment result = paymentService.getById(id);

    assertNotNull(result);
    verify(paymentRepository).findById(id);
  }
}
