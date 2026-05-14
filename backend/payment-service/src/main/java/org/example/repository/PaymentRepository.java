package org.example.repository;

import org.example.domain.Payment;
import org.example.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
  List<Payment> findByBookingId(Long bookingId);

  List<Payment> findByStatus(PaymentStatus status);
}
