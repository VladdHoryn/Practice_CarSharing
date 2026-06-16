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

  // --- canBeProcessed ---
  @Test
  void canBeProcessed_createdStatus_shouldReturnTrue() {
    payment.setStatus(PaymentStatus.CREATED);
    assertThat(payment.canBeProcessed()).isTrue();
  }

  @Test
  void canBeProcessed_pendingStatus_shouldReturnTrue() {
    payment.setStatus(PaymentStatus.PENDING);
    assertThat(payment.canBeProcessed()).isTrue();
  }

  @Test
  void canBeProcessed_successStatus_shouldReturnFalse() {
    payment.setStatus(PaymentStatus.SUCCESS);
    assertThat(payment.canBeProcessed()).isFalse();
  }

  @Test
  void canBeProcessed_cancelledStatus_shouldReturnFalse() {
    payment.setStatus(PaymentStatus.CANCELLED);
    assertThat(payment.canBeProcessed()).isFalse();
  }

  // --- markAsPending ---
  @Test
  void markAsPending_fromCreated_shouldBecomePending() {
    payment.markAsPending();
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  void markAsPending_fromProcessing_shouldThrow() {
    payment.setStatus(PaymentStatus.PROCESSING);
    assertThatThrownBy(payment::markAsPending)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Invalid payment status transition");
  }

  @Test
  void markAsPending_fromSuccess_shouldThrow() {
    payment.setStatus(PaymentStatus.SUCCESS);
    assertThatThrownBy(payment::markAsPending)
      .isInstanceOf(IllegalStateException.class);
  }

  // --- markAsProcessing ---
  @Test
  void markAsProcessing_fromPending_shouldBecomeProcessing() {
    payment.setStatus(PaymentStatus.PENDING);
    payment.markAsProcessing();
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
  }

  @Test
  void markAsProcessing_fromCreated_shouldThrow() {
    assertThatThrownBy(payment::markAsProcessing)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Invalid payment status transition");
  }

  // --- markAsSuccess ---
  @Test
  void markAsSuccess_fromProcessing_shouldBecomeSuccess() {
    payment.setStatus(PaymentStatus.PROCESSING);
    payment.markAsSuccess();
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
  }

  @Test
  void markAsSuccess_fromPending_shouldThrow() {
    payment.setStatus(PaymentStatus.PENDING);
    assertThatThrownBy(payment::markAsSuccess)
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void markAsSuccess_fromCreated_shouldThrow() {
    assertThatThrownBy(payment::markAsSuccess)
      .isInstanceOf(IllegalStateException.class);
  }

  // --- markAsFailed ---
  @Test
  void markAsFailed_fromCreated_shouldBecomeFailed() {
    payment.markAsFailed();
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
  }

  @Test
  void markAsFailed_fromPending_shouldBecomeFailed() {
    payment.setStatus(PaymentStatus.PENDING);
    payment.markAsFailed();
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
  }

  @Test
  void markAsFailed_fromProcessing_shouldBecomeFailed() {
    payment.setStatus(PaymentStatus.PROCESSING);
    payment.markAsFailed();
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
  }

  @Test
  void markAsFailed_fromSuccess_shouldThrow() {
    payment.setStatus(PaymentStatus.SUCCESS);
    assertThatThrownBy(payment::markAsFailed)
      .isInstanceOf(IllegalStateException.class);
  }

  // --- cancel ---
  @Test
  void cancel_fromCreated_shouldBecomeCancelled() {
    payment.cancel();
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
  }

  @Test
  void cancel_fromPending_shouldBecomeCancelled() {
    payment.setStatus(PaymentStatus.PENDING);
    payment.cancel();
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
  }

  @Test
  void cancel_fromProcessing_shouldThrow() {
    payment.setStatus(PaymentStatus.PROCESSING);
    assertThatThrownBy(payment::cancel)
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void cancel_fromSuccess_shouldThrow() {
    payment.setStatus(PaymentStatus.SUCCESS);
    assertThatThrownBy(payment::cancel)
      .isInstanceOf(IllegalStateException.class);
  }

  // --- refund ---
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
