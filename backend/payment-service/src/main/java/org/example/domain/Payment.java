package org.example.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @NotNull(message = "Payment status cannot be null")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @PastOrPresent(message = "Payment date cannot be in the future")
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "provider_payment_id")
    private String providerPaymentId;

    @NotBlank
    @Column(name = "currency")
    private String currency;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }

        if (status == null) {
            status = PaymentStatus.CREATED;
        }

        log.info("Creating payment: bookingId={}, amount={}, status={}", bookingId, amount, status);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void refund() {
        if (status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only SUCCESS payments can be refunded");
        }

        this.status = PaymentStatus.REFUNDED;

        log.info("Payment {} refunded", id);
    }

    public void changeStatus(PaymentStatus newStatus) {
        log.info("Payment id={} status changed from {} to {}", this.id, this.status, newStatus);

        this.status = newStatus;
    }
}
