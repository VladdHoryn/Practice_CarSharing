package org.example.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class PaymentTest {

  private Payment payment;

  @BeforeEach
  void setUp() {
    payment = new Payment();
    payment.setBookingId(1L);
    payment.setAmount(BigDecimal.valueOf(500));
    payment.setMethod(PaymentMethod.CARD);
    payment.setCurrency("UAH");
    payment.setStatus(PaymentStatus.CREATED);
    payment.setPaymentDate(LocalDateTime.now());
    payment.setIdempotencyKey("test-key-123");
  }

  @Test
  void refund_fromSuccess_shouldBecomeRefunded() {
    payment.setStatus(PaymentStatus.SUCCESS);
    payment.refund();
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
  }

  @Test
  void refund_fromCreated_shouldThrow() {
    assertThatThrownBy(payment::refund)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Only SUCCESS payments can be refunded");
  }

  @Test
  void refund_fromPending_shouldThrow() {
    payment.setStatus(PaymentStatus.PENDING);
    assertThatThrownBy(payment::refund)
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void refund_fromCancelled_shouldThrow() {
    payment.setStatus(PaymentStatus.CANCELLED);
    assertThatThrownBy(payment::refund)
      .isInstanceOf(IllegalStateException.class);
  }
}
