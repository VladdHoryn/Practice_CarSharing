package org.example.application;

import jakarta.persistence.EntityNotFoundException;
import org.example.domain.Payment;
import org.example.domain.PaymentMethod;
import org.example.domain.PaymentStatus;
import org.example.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentApplicationServiceTest {

  @Mock
  private PaymentRepository paymentRepository;

  @InjectMocks
  private PaymentApplicationService paymentService;

  private Payment payment;

  @BeforeEach
  void setUp() {
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
  void createPayment_shouldSaveAndReturn() {
    when(paymentRepository.save(any())).thenReturn(payment);

    Payment result = paymentService.createPayment(
      1L, BigDecimal.valueOf(500), PaymentMethod.CARD, "UAH");

    assertThat(result).isNotNull();
    verify(paymentRepository).save(any(Payment.class));
  }

  @Test
  void getById_exists_shouldReturn() {
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

    Payment result = paymentService.getById(1L);

    assertThat(result.getId()).isEqualTo(1L);
  }

  @Test
  void getById_notFound_shouldThrow() {
    when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.getById(999L))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessageContaining("Payment not found with id=999");
  }

  @Test
  void getAll_shouldReturnAll() {
    when(paymentRepository.findAll()).thenReturn(List.of(payment));

    List<Payment> result = paymentService.getAll();

    assertThat(result).hasSize(1);
  }

  @Test
  void updatePayment_shouldUpdateFields() {
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any())).thenReturn(payment);

    Payment result = paymentService.updatePayment(
      1L, BigDecimal.valueOf(1000), PaymentMethod.GOOGLE_PAY, "USD");

    assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    assertThat(result.getMethod()).isEqualTo(PaymentMethod.GOOGLE_PAY);
    assertThat(result.getCurrency()).isEqualTo("USD");
  }

  @Test
  void updatePayment_nullFields_shouldNotUpdate() {
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any())).thenReturn(payment);

    Payment result = paymentService.updatePayment(1L, null, null, null);

    assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
    assertThat(result.getMethod()).isEqualTo(PaymentMethod.CARD);
  }

  @Test
  void deletePayment_shouldDelete() {
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

    paymentService.deletePayment(1L);

    verify(paymentRepository).delete(payment);
  }

  @Test
  void deletePayment_notFound_shouldThrow() {
    when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.deletePayment(999L))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void refundPayment_fromSuccess_shouldReturnRefunded() {
    payment.setStatus(PaymentStatus.SUCCESS);
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any())).thenReturn(payment);

    Payment result = paymentService.refundPayment(1L);

    assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
  }

  @Test
  void refundPayment_fromCreated_shouldThrow() {
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

    assertThatThrownBy(() -> paymentService.refundPayment(1L))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Only SUCCESS payments can be refunded");
  }
}
