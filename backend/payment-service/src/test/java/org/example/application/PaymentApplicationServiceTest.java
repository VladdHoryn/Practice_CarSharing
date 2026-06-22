package org.example.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.example.domain.Payment;
import org.example.domain.PaymentMethod;
import org.example.domain.PaymentStatus;
import org.example.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentApplicationServiceTest {

    @Mock private PaymentRepository paymentRepository;

    @InjectMocks private PaymentApplicationService paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(1L);
        payment.setBookingId(10L);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setMethod(PaymentMethod.CARD);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCurrency("USD");
    }

    @Nested
    @DisplayName("createPayment()")
    class CreatePaymentTests {

        @Test
        @DisplayName("успішно створює платіж")
        void shouldCreatePaymentSuccessfully() {
            when(paymentRepository.save(any())).thenReturn(payment);
            Payment result =
                    paymentService.createPayment(
                            10L, BigDecimal.valueOf(500), PaymentMethod.CARD, "USD");
            assertThat(result).isNotNull();
            verify(paymentRepository).save(any());
        }

        @Test
        @DisplayName("встановлює idempotencyKey при створенні")
        void shouldSetIdempotencyKey() {
            when(paymentRepository.save(any()))
                    .thenAnswer(
                            inv -> {
                                Payment p = inv.getArgument(0);
                                assertThat(p.getIdempotencyKey()).isNotNull();
                                return p;
                            });
            paymentService.createPayment(
                    10L, BigDecimal.valueOf(100), PaymentMethod.GOOGLE_PAY, "EUR");
        }

        @Test
        @DisplayName("встановлює bookingId, amount, method, currency")
        void shouldSetAllFields() {
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            Payment result =
                    paymentService.createPayment(
                            42L, BigDecimal.valueOf(999), PaymentMethod.CARD, "UAH");
            assertThat(result.getBookingId()).isEqualTo(42L);
            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(999));
            assertThat(result.getMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(result.getCurrency()).isEqualTo("UAH");
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("повертає платіж за ID")
        void shouldReturnPaymentById() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            Payment result = paymentService.getById(1L);
            assertThat(result.getId()).isEqualTo(1L);
            verify(paymentRepository).findById(1L);
        }

        @Test
        @DisplayName("кидає EntityNotFoundException якщо не знайдено")
        void shouldThrowWhenPaymentNotFound() {
            when(paymentRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> paymentService.getById(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Payment not found");
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("повертає всі платежі")
        void shouldReturnAllPayments() {
            when(paymentRepository.findAll()).thenReturn(List.of(payment, new Payment()));
            assertThat(paymentService.getAll()).hasSize(2);
        }

        @Test
        @DisplayName("повертає порожній список")
        void shouldReturnEmptyList() {
            when(paymentRepository.findAll()).thenReturn(Collections.emptyList());
            assertThat(paymentService.getAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updatePayment()")
    class UpdatePaymentTests {

        @Test
        @DisplayName("оновлює суму платежу")
        void shouldUpdateAmount() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            Payment result = paymentService.updatePayment(1L, BigDecimal.valueOf(999), null, null);
            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(999));
        }

        @Test
        @DisplayName("оновлює метод оплати")
        void shouldUpdateMethod() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            Payment result = paymentService.updatePayment(1L, null, PaymentMethod.GOOGLE_PAY, null);
            assertThat(result.getMethod()).isEqualTo(PaymentMethod.GOOGLE_PAY);
        }

        @Test
        @DisplayName("оновлює валюту")
        void shouldUpdateCurrency() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            Payment result = paymentService.updatePayment(1L, null, null, "EUR");
            assertThat(result.getCurrency()).isEqualTo("EUR");
        }

        @Test
        @DisplayName("не змінює поля якщо вони null")
        void shouldNotChangeFieldsWhenNull() {
            payment.setAmount(BigDecimal.valueOf(500));
            payment.setMethod(PaymentMethod.CARD);
            payment.setCurrency("USD");
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            Payment result = paymentService.updatePayment(1L, null, null, null);
            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
            assertThat(result.getMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(result.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("кидає EntityNotFoundException якщо не знайдено")
        void shouldThrowWhenPaymentNotFound() {
            when(paymentRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(
                            () ->
                                    paymentService.updatePayment(
                                            99L, BigDecimal.valueOf(100), null, null))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deletePayment()")
    class DeletePaymentTests {

        @Test
        @DisplayName("успішно видаляє платіж")
        void shouldDeletePaymentSuccessfully() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            doNothing().when(paymentRepository).delete(payment);
            paymentService.deletePayment(1L);
            verify(paymentRepository).delete(payment);
        }

        @Test
        @DisplayName("кидає EntityNotFoundException якщо не знайдено")
        void shouldThrowWhenPaymentNotFound() {
            when(paymentRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> paymentService.deletePayment(99L))
                    .isInstanceOf(EntityNotFoundException.class);
            verify(paymentRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("refundPayment()")
    class RefundPaymentTests {

        @Test
        @DisplayName("успішно повертає SUCCESS платіж")
        void shouldRefundSuccessPayment() {
            payment.setStatus(PaymentStatus.SUCCESS);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            Payment result = paymentService.refundPayment(1L);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("кидає IllegalStateException для не-SUCCESS платежу")
        void shouldThrowForNonSuccessPayment() {
            payment.setStatus(PaymentStatus.CREATED);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            assertThatThrownBy(() -> paymentService.refundPayment(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only SUCCESS payments can be refunded");
        }

        @Test
        @DisplayName("кидає EntityNotFoundException якщо не знайдено")
        void shouldThrowWhenPaymentNotFound() {
            when(paymentRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> paymentService.refundPayment(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatusTests {

        @Test
        @DisplayName("успішно змінює статус платежу")
        void shouldChangeStatus() {
            payment.setStatus(PaymentStatus.CREATED);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            paymentService.changeStatus(1L, PaymentStatus.SUCCESS);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @Test
        @DisplayName("кидає EntityNotFoundException якщо не знайдено")
        void shouldThrowWhenNotFound() {
            when(paymentRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> paymentService.changeStatus(99L, PaymentStatus.SUCCESS))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
