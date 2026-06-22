package org.example.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PaymentTest {

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(1L);
        payment.setBookingId(10L);
        payment.setAmount(BigDecimal.valueOf(500.00));
        payment.setMethod(PaymentMethod.CARD);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCurrency("USD");
        payment.setPaymentDate(LocalDateTime.now());
    }

    @Test
    void shouldSetAndGetId() {
        payment.setId(99L);
        assertThat(payment.getId()).isEqualTo(99L);
    }

    @Test
    void shouldSetAndGetBookingId() {
        payment.setBookingId(42L);
        assertThat(payment.getBookingId()).isEqualTo(42L);
    }

    @Test
    void shouldSetAndGetAmount() {
        payment.setAmount(BigDecimal.valueOf(999.99));
        assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(999.99));
    }

    @Test
    void shouldSetAndGetStatus() {
        payment.setStatus(PaymentStatus.CREATED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CREATED);
    }

    @Test
    void shouldSetAndGetCurrency() {
        payment.setCurrency("EUR");
        assertThat(payment.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void shouldSetAndGetIdempotencyKey() {
        payment.setIdempotencyKey("key-123");
        assertThat(payment.getIdempotencyKey()).isEqualTo("key-123");
    }

    @Test
    void shouldSetAndGetClientSecret() {
        payment.setClientSecret("secret-abc");
        assertThat(payment.getClientSecret()).isEqualTo("secret-abc");
    }

    @Test
    void shouldSetAndGetProviderPaymentId() {
        payment.setProviderPaymentId("prov-123");
        assertThat(payment.getProviderPaymentId()).isEqualTo("prov-123");
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    @DisplayName("підтримує всі методи оплати")
    void shouldSupportAllPaymentMethods(PaymentMethod method) {
        payment.setMethod(method);
        assertThat(payment.getMethod()).isEqualTo(method);
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    @DisplayName("підтримує всі статуси оплати")
    void shouldSupportAllPaymentStatuses(PaymentStatus status) {
        payment.setStatus(status);
        assertThat(payment.getStatus()).isEqualTo(status);
    }

    @Nested
    @DisplayName("prePersist()")
    class PrePersistTests {

        @Test
        @DisplayName("встановлює createdAt і updatedAt")
        void shouldSetTimestamps() {
            Payment p = new Payment();
            p.setBookingId(1L);
            p.setAmount(BigDecimal.valueOf(100));
            p.setMethod(PaymentMethod.CARD);
            p.setCurrency("USD");
            p.prePersist();
            assertThat(p.getCreatedAt()).isNotNull();
            assertThat(p.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("встановлює статус CREATED якщо не заданий")
        void shouldDefaultStatusToCreated() {
            Payment p = new Payment();
            p.setBookingId(1L);
            p.setAmount(BigDecimal.valueOf(100));
            p.setMethod(PaymentMethod.CARD);
            p.setCurrency("USD");
            p.prePersist();
            assertThat(p.getStatus()).isEqualTo(PaymentStatus.CREATED);
        }

        @Test
        @DisplayName("не перезатирає вже встановлений статус")
        void shouldNotOverrideExistingStatus() {
            Payment p = new Payment();
            p.setStatus(PaymentStatus.SUCCESS);
            p.prePersist();
            assertThat(p.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @Test
        @DisplayName("встановлює paymentDate якщо не задано")
        void shouldSetPaymentDateIfNull() {
            Payment p = new Payment();
            p.setBookingId(1L);
            p.setAmount(BigDecimal.valueOf(100));
            p.setMethod(PaymentMethod.CARD);
            p.setCurrency("USD");
            p.prePersist();
            assertThat(p.getPaymentDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("preUpdate()")
    class PreUpdateTests {

        @Test
        @DisplayName("оновлює updatedAt")
        void shouldUpdateTimestamp() {
            payment.prePersist();
            LocalDateTime before = payment.getUpdatedAt();
            payment.preUpdate();
            assertThat(payment.getUpdatedAt()).isAfterOrEqualTo(before);
        }
    }

    @Nested
    @DisplayName("refund()")
    class RefundTests {

        @Test
        @DisplayName("успішно повертає SUCCESS платіж")
        void shouldRefundSuccessPayment() {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.refund();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("кидає IllegalStateException для CREATED статусу")
        void shouldThrowForCreatedStatus() {
            payment.setStatus(PaymentStatus.CREATED);
            assertThatThrownBy(() -> payment.refund())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only SUCCESS payments can be refunded");
        }

        @Test
        @DisplayName("кидає IllegalStateException для вже REFUNDED платежу")
        void shouldThrowForRefundedStatus() {
            payment.setStatus(PaymentStatus.REFUNDED);
            assertThatThrownBy(() -> payment.refund()).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("кидає IllegalStateException для FAILED платежу")
        void shouldThrowForFailedStatus() {
            payment.setStatus(PaymentStatus.FAILED);
            assertThatThrownBy(() -> payment.refund()).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatusTests {

        @Test
        @DisplayName("змінює статус без обмежень")
        void shouldChangeStatusUnconditionally() {
            payment.setStatus(PaymentStatus.CREATED);
            payment.changeStatus(PaymentStatus.SUCCESS);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @ParameterizedTest
        @EnumSource(PaymentStatus.class)
        @DisplayName("змінює статус на будь-який")
        void shouldChangeToAnyStatus(PaymentStatus status) {
            payment.changeStatus(status);
            assertThat(payment.getStatus()).isEqualTo(status);
        }
    }
}
