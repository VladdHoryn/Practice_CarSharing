package org.example.domain;

import jakarta.persistence.*;
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

  @Column(name = "booking_id", nullable = false)
  private Long bookingId;

  @Column(name = "amount", nullable = false)
  private Float amount;

  @Column(name = "method", nullable = false)
  private String method;

  @Column(name = "status", nullable = false)
  private PaymentStatus status;

  @Column(name = "transaction_id", nullable = false)
  private Long transactionId;

  @Column(name = "payment_date", nullable = false)
  private LocalDateTime paymentDate;
}
