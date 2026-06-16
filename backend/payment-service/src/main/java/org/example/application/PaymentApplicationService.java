package org.example.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.example.domain.Payment;
import org.example.domain.PaymentMethod;
import org.example.domain.PaymentStatus;
import org.example.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentApplicationService {

    private final PaymentRepository paymentRepository;

    public Payment createPayment(
            Long bookingId, BigDecimal amount, PaymentMethod method, String currency) {

        log.info(
                "Creating payment for bookingId={}, amount={}, method={}, currency={}",
                bookingId,
                amount,
                method,
                currency);

        Payment payment = new Payment();

        payment.setBookingId(bookingId);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setCurrency(currency);

        /*
         * Domain will handle:
         * - status = CREATED
         * - paymentDate
         * - createdAt
         * - updatedAt
         */
        payment.setIdempotencyKey(UUID.randomUUID().toString());

        return paymentRepository.save(payment);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Payment getById(Long id) {

        log.debug("Fetching payment by id={}", id);

        return paymentRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id=" + id));
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Payment> getAll() {

        log.debug("Fetching all payments");

        return paymentRepository.findAll();
    }

    public Payment updatePayment(
            Long id, BigDecimal amount, PaymentMethod method, String currency) {

        log.info("Updating payment id={}", id);

        Payment payment = getById(id);

        if (amount != null) {
            payment.setAmount(amount);
        }

        if (method != null) {
            payment.setMethod(method);
        }

        if (currency != null && !currency.isBlank()) {
            payment.setCurrency(currency);
        }

        // updatedAt handled by @PreUpdate
        return paymentRepository.save(payment);
    }

    public void deletePayment(Long id) {

        log.warn("Deleting payment id={}", id);

        Payment payment = getById(id);

        paymentRepository.delete(payment);
    }

    public Payment refundPayment(Long id) {

        log.info("Refunding payment {}", id);

        Payment payment = getById(id);

        payment.refund();

        return paymentRepository.save(payment);
    }

    public void changeStatus(Long id, PaymentStatus newStatus) {
        Payment payment = getById(id);

        payment.changeStatus(newStatus);
    }
}
