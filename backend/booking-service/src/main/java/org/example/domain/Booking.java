package org.example.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Duration;
import java.time.LocalDateTime;
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
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @NotNull(message = "Total price is required")
    @Positive(message = "Total price must be positive")
    @Column(name = "total_price", nullable = false)
    private Float totalPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

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

    private void validateCanModify() {
        if (status == BookingStatus.CANCELLED || status == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot modify finished booking");
        }
    }

    public void submitForProcessing() {
        log.info("Booking id={} -> PENDING", id);

        if (status != BookingStatus.CREATED) {
            throw new IllegalStateException("Only CREATED booking can be submitted");
        }

        this.status = BookingStatus.PENDING;
    }

    public void confirm() {
        log.info("Booking id={} -> CONFIRMED", id);

        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING booking can be confirmed");
        }

        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        log.info("Booking id={} -> CANCELLED", id);

        if (status == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed booking");
        }

        this.status = BookingStatus.CANCELLED;
    }

    public void complete() {
        log.info("Booking id={} -> COMPLETED", id);

        if (status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED booking can be completed");
        }

        this.status = BookingStatus.COMPLETED;
    }

    public void calculateTotalPrice(float pricePerDay) {
        long days = Duration.between(startDate, endDate).toDays();

        if (days <= 0) {
            throw new IllegalStateException("Booking must be at least 1 day");
        }

        this.totalPrice = days * pricePerDay;
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
