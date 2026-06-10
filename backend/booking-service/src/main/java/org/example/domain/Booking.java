package org.example.domain;

import java.math.BigDecimal;
import java.time.Duration;
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
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull(message = "Car ID is required")
    @Column(name = "car_id", nullable = false)
    private Long carId;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @NotNull(message = "Status is required")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @NotNull(message = "Total price is required")
    @Positive(message = "Total price must be positive")
    @Digits(integer = 10, fraction = 2)
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "cancel_deadline", nullable = false)
    private LocalDateTime cancelDeadline;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = BookingStatus.CREATED;
        }

        validateDates();
    }

    @PreUpdate
    protected void onUpdate() {
        validateDates();
    }

    private void validateDates() {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }

    public void cancel() {
        log.info("Booking id={} -> CANCELLED", id);

        if (status == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed booking");
        }

        if (status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            throw new IllegalStateException(
                    "Cancellation deadline has expired. Cancel was allowed until: "
                            + cancelDeadline);
        }

        this.status = BookingStatus.CANCELLED;
    }

  public void changeStatus(BookingStatus newStatus){
    log.info("Booking id={} changes status from {} to {}", id, this.status, newStatus);

    this.status = newStatus;
  }

    public void calculateTotalPrice(BigDecimal pricePerDay) {
        long days = Duration.between(startDate, endDate).toDays();

        if (days <= 0) {
            throw new IllegalStateException("Booking must be at least 1 day");
        }

        this.totalPrice = pricePerDay.multiply(BigDecimal.valueOf(days));
    }

    public boolean isActive() {
        return status == BookingStatus.CONFIRMED;
    }

    public boolean isPending() {
        return status == BookingStatus.PENDING;
    }

    public boolean isFinished() {
        return status == BookingStatus.CANCELLED || status == BookingStatus.COMPLETED;
    }
}
