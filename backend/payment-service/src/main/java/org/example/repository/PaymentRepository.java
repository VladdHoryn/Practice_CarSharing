package org.example.repository;

import java.util.List;

import org.example.domain.Payment;
import org.example.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);

    List<Payment> findByStatus(PaymentStatus status);
}
