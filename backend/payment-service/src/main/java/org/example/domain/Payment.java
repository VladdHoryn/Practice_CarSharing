package org.example.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Booking id cannot be null")
  @Column(name = "booking_id", nullable = false)
  private Long bookingId;

  @NotNull(message = "Total price is required")
  @Positive(message = "Total price must be positive")
  @Digits(integer = 10, fraction = 2)
  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  private Float  amount;

  @NotBlank(message = "Payment method cannot be blank")
  @Enumerated(EnumType.STRING)
  @Column(name = "method", nullable = false)
  private PaymentMethod method;

  @NotNull(message = "Payment status cannot be null")
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PaymentStatus status;

  @NotNull(message = "Transaction id cannot be null")
  @Column(name = "transaction_id", nullable = false)
  private Long transactionId;

  @PastOrPresent(message = "Payment date cannot be in the future")
  @Column(name = "payment_date", nullable = false)
  private LocalDateTime paymentDate;

  @PrePersist
  public void prePersist() {
    if (paymentDate == null) {
      paymentDate = LocalDateTime.now();
    }

    if (status == null) {
      status = PaymentStatus.CREATED;
    }

    log.info(
      "Creating payment: bookingId={}, amount={}, status={}",
      bookingId,
      amount,
      status
    );
  }

  public boolean canBeProcessed() {
    return status == PaymentStatus.CREATED
      || status == PaymentStatus.PENDING;
  }

  public void markAsSuccess() {
    validateTransition(PaymentStatus.SUCCESS);

    this.status = PaymentStatus.SUCCESS;

    log.info(
      "Payment {} successfully completed",
      id
    );
  }

  public void markAsFailed() {
    validateTransition(PaymentStatus.FAILED);

    this.status = PaymentStatus.FAILED;

    log.warn(
      "Payment {} failed",
      id
    );
  }

  public void cancel() {
    validateTransition(PaymentStatus.CANCELLED);

    this.status = PaymentStatus.CANCELLED;

    log.warn(
      "Payment {} cancelled",
      id
    );
  }

  public void refund() {
    if (status != PaymentStatus.SUCCESS) {
      throw new IllegalStateException(
        "Only SUCCESS payments can be refunded"
      );
    }

    this.status = PaymentStatus.REFUNDED;

    log.info(
      "Payment {} refunded",
      id
    );
  }

  private void validateTransition(PaymentStatus targetStatus) {

    boolean valid = switch (status) {

      case CREATED ->
        targetStatus == PaymentStatus.PENDING
          || targetStatus == PaymentStatus.CANCELLED
          || targetStatus == PaymentStatus.FAILED;

      case PENDING ->
        targetStatus == PaymentStatus.PROCESSING
          || targetStatus == PaymentStatus.CANCELLED
          || targetStatus == PaymentStatus.FAILED;

      case PROCESSING ->
        targetStatus == PaymentStatus.SUCCESS
          || targetStatus == PaymentStatus.FAILED;

      case SUCCESS ->
        targetStatus == PaymentStatus.REFUNDED;

      case FAILED, CANCELLED, REFUNDED ->
        false;
    };

    if (!valid) {
      throw new IllegalStateException(
        String.format(
          "Invalid payment status transition: %s -> %s",
          status,
          targetStatus
        )
      );
    }
  }
}
