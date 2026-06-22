package org.example.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.example.application.PaymentApplicationService;
import org.example.domain.Payment;
import org.example.dto.ChangePaymentStatus;
import org.example.dto.CreatePaymentRequest;
import org.example.dto.UpdatePaymentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payment/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    @Operation(summary = "Create payment", description = "Creates a new payment for a booking. Accessible by RENTER or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Payment created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('RENTER', 'ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody @Valid CreatePaymentRequest request) {

        log.info("REST request to create payment");

        Payment payment =
                paymentApplicationService.createPayment(
                        request.bookingId(),
                        request.amount(),
                        request.method(),
                        request.currency());

        return ResponseEntity.created(URI.create("/payment/v1/" + payment.getId())).body(payment);
    }

    @Operation(summary = "Get payment by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment found"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getById(@PathVariable Long id) {

        log.debug("REST request to get payment {}", id);

        return ResponseEntity.ok(paymentApplicationService.getById(id));
    }

    @Operation(summary = "Get all payments", description = "Returns all payments. Accessible by ADMINISTRATOR only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of payments returned"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping
    public ResponseEntity<List<Payment>> getAll() {

        log.debug("REST request to get all payments");

        return ResponseEntity.ok(paymentApplicationService.getAll());
    }

    @Operation(summary = "Update payment", description = "Updates payment amount, method or currency. Accessible by ADMINISTRATOR only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment updated"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(
            @PathVariable Long id, @RequestBody @Valid UpdatePaymentRequest request) {

        log.info("REST request to update payment {}", id);

        Payment updatedPayment =
                paymentApplicationService.updatePayment(
                        id, request.amount(), request.method(), request.currency());

        return ResponseEntity.ok(updatedPayment);
    }

    @Operation(summary = "Delete payment", description = "Deletes a payment. Accessible by ADMINISTRATOR only.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Payment deleted"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePayment(@PathVariable Long id) {

        log.warn("REST request to delete payment {}", id);

        paymentApplicationService.deletePayment(id);
    }

    @Operation(summary = "Refund payment", description = "Processes a refund. Accessible by RENTER or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment refunded"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('RENTER', 'ADMINISTRATOR')")
    @PatchMapping("/{id}/refund")
    public ResponseEntity<Payment> refundPayment(@PathVariable Long id) {

        return ResponseEntity.ok(paymentApplicationService.refundPayment(id));
    }

    @Operation(summary = "Change payment status (admin)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Status changed"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{id}/status/change")
    public ResponseEntity<Void> changePaymentStatus(
            @PathVariable Long id, @Valid @RequestBody ChangePaymentStatus request) {
        paymentApplicationService.changeStatus(id, request.newStatus());

        return ResponseEntity.noContent().build();
    }
}
