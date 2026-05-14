package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.PaymentApplicationService;
import org.example.domain.Payment;
import org.example.dto.CreatePaymentRequest;
import org.example.dto.UpdatePaymentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("payment/v1")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

  private final PaymentApplicationService paymentApplicationService;

  @PostMapping
  public ResponseEntity<Payment> createPayment(
    @RequestBody @Valid CreatePaymentRequest request) {

    log.info("REST request to create payment");

    Payment payment =
      paymentApplicationService.createPayment(
        request.bookingId(),
        request.amount(),
        request.method());

    return ResponseEntity
      .created(URI.create("/payments/" + payment.getId()))
      .body(payment);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Payment> getById(@PathVariable Long id) {

    log.debug("REST request to get payment {}", id);

    return ResponseEntity.ok(
      paymentApplicationService.getById(id));
  }

  @GetMapping
  public ResponseEntity<List<Payment>> getAll() {

    log.debug("REST request to get all payments");

    return ResponseEntity.ok(
      paymentApplicationService.getAll());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Payment> updatePayment(
    @PathVariable Long id,
    @RequestBody @Valid UpdatePaymentRequest request) {

    log.info("REST request to update payment {}", id);

    Payment updatedPayment =
      paymentApplicationService.updatePayment(
        id,
        request.amount(),
        request.method());

    return ResponseEntity.ok(updatedPayment);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePayment(@PathVariable Long id) {

    log.warn("REST request to delete payment {}", id);

    paymentApplicationService.deletePayment(id);
  }

  @PatchMapping("/{id}/pending")
  public ResponseEntity<Payment> markAsPending(@PathVariable Long id) {

    return ResponseEntity.ok(
      paymentApplicationService.markAsPending(id));
  }

  @PatchMapping("/{id}/processing")
  public ResponseEntity<Payment> markAsProcessing(@PathVariable Long id) {

    return ResponseEntity.ok(
      paymentApplicationService.markAsProcessing(id));
  }

  @PatchMapping("/{id}/success")
  public ResponseEntity<Payment> markAsSuccess(@PathVariable Long id) {

    return ResponseEntity.ok(
      paymentApplicationService.markAsSuccess(id));
  }

  @PatchMapping("/{id}/failed")
  public ResponseEntity<Payment> markAsFailed(@PathVariable Long id) {

    return ResponseEntity.ok(
      paymentApplicationService.markAsFailed(id));
  }

  @PatchMapping("/{id}/cancel")
  public ResponseEntity<Payment> cancelPayment(@PathVariable Long id) {

    return ResponseEntity.ok(
      paymentApplicationService.cancelPayment(id));
  }

  @PatchMapping("/{id}/refund")
  public ResponseEntity<Payment> refundPayment(@PathVariable Long id) {

    return ResponseEntity.ok(
      paymentApplicationService.refundPayment(id));
  }
}
