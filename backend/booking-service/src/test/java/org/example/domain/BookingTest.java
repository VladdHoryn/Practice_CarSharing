package org.example.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BookingTest {

    private Booking booking;

    @BeforeEach
    void setUp() {
        booking = new Booking();
        booking.setUserId(1L);
        booking.setCarId(10L);
        booking.setStartDate(LocalDateTime.now().plusDays(5));
        booking.setEndDate(LocalDateTime.now().plusDays(8));
        booking.setTotalPrice(BigDecimal.valueOf(300));
        booking.setCancelDeadline(LocalDateTime.now().plusDays(3));
    }

    @Nested
    @DisplayName("onCreate() lifecycle hook")
    class OnCreateTests {

        @Test
        @DisplayName("встановлює createdAt і updatedAt")
        void setsTimestamps() {
            booking.onCreate();

            assertThat(booking.getCreatedAt()).isNotNull();
            assertThat(booking.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("встановлює статус CREATED, якщо статус не задано")
        void defaultsStatusToCreated() {
            booking.setStatus(null);

            booking.onCreate();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CREATED);
        }

        @Test
        @DisplayName("не перезатирає статус, якщо він уже заданий")
        void doesNotOverrideExistingStatus() {
            booking.setStatus(BookingStatus.CONFIRMED);

            booking.onCreate();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        @DisplayName("кидає IllegalArgumentException, якщо endDate не пізніше за startDate")
        void throwsWhenEndDateNotAfterStartDate() {
            LocalDateTime now = LocalDateTime.now();
            booking.setStartDate(now);
            booking.setEndDate(now.minusHours(1));

            assertThatThrownBy(() -> booking.onCreate())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("End date must be after start date");
        }

        @Test
        @DisplayName("кидає IllegalArgumentException, якщо endDate рівна startDate")
        void throwsWhenEndDateEqualsStartDate() {
            LocalDateTime now = LocalDateTime.now();
            booking.setStartDate(now);
            booking.setEndDate(now);

            assertThatThrownBy(() -> booking.onCreate())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("onUpdate() lifecycle hook")
    class OnUpdateTests {

        @Test
        @DisplayName("проходить без помилок для коректних дат")
        void validDatesDoNotThrow() {
            booking.onCreate();

            org.assertj.core.api.Assertions.assertThatCode(() -> booking.onUpdate())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("кидає виняток, якщо дати стали некоректними")
        void invalidDatesThrow() {
            booking.setStartDate(LocalDateTime.now());
            booking.setEndDate(LocalDateTime.now().minusDays(1));

            assertThatThrownBy(() -> booking.onUpdate())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("cancel()")
    class CancelTests {

        @Test
        @DisplayName("переводить статус у CANCELLED, якщо дедлайн не минув")
        void cancelsWhenBeforeDeadline() {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setCancelDeadline(LocalDateTime.now().plusDays(1));

            booking.cancel();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }

        @Test
        @DisplayName("кидає IllegalStateException, якщо бронювання вже COMPLETED")
        void throwsWhenAlreadyCompleted() {
            booking.setStatus(BookingStatus.COMPLETED);
            booking.setCancelDeadline(LocalDateTime.now().plusDays(1));

            assertThatThrownBy(() -> booking.cancel())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot cancel completed booking");
        }

        @Test
        @DisplayName("кидає IllegalStateException, якщо бронювання вже CANCELLED")
        void throwsWhenAlreadyCancelled() {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelDeadline(LocalDateTime.now().plusDays(1));

            assertThatThrownBy(() -> booking.cancel())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already cancelled");
        }

        @Test
        @DisplayName("кидає IllegalStateException, якщо дедлайн скасування минув")
        void throwsWhenDeadlineExpired() {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setCancelDeadline(LocalDateTime.now().minusDays(1));

            assertThatThrownBy(() -> booking.cancel())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cancellation deadline has expired");
        }

        @Test
        @DisplayName("дозволяє скасувати бронювання зі статусом PENDING")
        void cancelsPendingBooking() {
            booking.setStatus(BookingStatus.PENDING);
            booking.setCancelDeadline(LocalDateTime.now().plusDays(1));

            booking.cancel();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatusTests {

        @Test
        @DisplayName("змінює статус на переданий, без додаткових перевірок")
        void changesStatusUnconditionally() {
            booking.setStatus(BookingStatus.CREATED);

            booking.changeStatus(BookingStatus.CONFIRMED);

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        @DisplayName("дозволяє встановити той самий статус повторно")
        void allowsSettingSameStatus() {
            booking.setStatus(BookingStatus.CONFIRMED);

            booking.changeStatus(BookingStatus.CONFIRMED);

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }
    }

    @Nested
    @DisplayName("calculateTotalPrice()")
    class CalculateTotalPriceTests {

        @Test
        @DisplayName("розраховує ціну як pricePerDay * кількість днів")
        void calculatesPriceForMultipleDays() {
            booking.setStartDate(LocalDateTime.of(2026, 1, 1, 10, 0));
            booking.setEndDate(LocalDateTime.of(2026, 1, 4, 10, 0));

            booking.calculateTotalPrice(BigDecimal.valueOf(100));

            assertThat(booking.getTotalPrice()).isEqualTo(BigDecimal.valueOf(300));
        }

        @Test
        @DisplayName("розраховує ціну для рівно одного дня")
        void calculatesPriceForOneDay() {
            booking.setStartDate(LocalDateTime.of(2026, 1, 1, 10, 0));
            booking.setEndDate(LocalDateTime.of(2026, 1, 2, 10, 0));

            booking.calculateTotalPrice(BigDecimal.valueOf(50));

            assertThat(booking.getTotalPrice()).isEqualTo(BigDecimal.valueOf(50));
        }

        @Test
        @DisplayName("кидає IllegalStateException, якщо тривалість менше 1 дня")
        void throwsWhenLessThanOneDay() {
            LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
            booking.setStartDate(base);
            booking.setEndDate(base.plusHours(5));

            assertThatThrownBy(() -> booking.calculateTotalPrice(BigDecimal.valueOf(100)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("at least 1 day");
        }

        @Test
        @DisplayName("кидає IllegalStateException, якщо дати рівні (0 днів)")
        void throwsWhenZeroDays() {
            LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
            booking.setStartDate(base);
            booking.setEndDate(base);

            assertThatThrownBy(() -> booking.calculateTotalPrice(BigDecimal.valueOf(100)))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("isActive() / isPending() / isFinished()")
    class StatusPredicateTests {

        @Test
        @DisplayName("isActive() true тільки для CONFIRMED")
        void isActiveOnlyForConfirmed() {
            booking.setStatus(BookingStatus.CONFIRMED);
            assertThat(booking.isActive()).isTrue();

            booking.setStatus(BookingStatus.CREATED);
            assertThat(booking.isActive()).isFalse();
        }

        @Test
        @DisplayName("isPending() true тільки для PENDING")
        void isPendingOnlyForPending() {
            booking.setStatus(BookingStatus.PENDING);
            assertThat(booking.isPending()).isTrue();

            booking.setStatus(BookingStatus.CONFIRMED);
            assertThat(booking.isPending()).isFalse();
        }

        @Test
        @DisplayName("isFinished() true для CANCELLED")
        void isFinishedForCancelled() {
            booking.setStatus(BookingStatus.CANCELLED);
            assertThat(booking.isFinished()).isTrue();
        }

        @Test
        @DisplayName("isFinished() true для COMPLETED")
        void isFinishedForCompleted() {
            booking.setStatus(BookingStatus.COMPLETED);
            assertThat(booking.isFinished()).isTrue();
        }

        @Test
        @DisplayName("isFinished() false для CREATED/PENDING/CONFIRMED")
        void isFinishedFalseForActiveStatuses() {
            booking.setStatus(BookingStatus.CREATED);
            assertThat(booking.isFinished()).isFalse();

            booking.setStatus(BookingStatus.PENDING);
            assertThat(booking.isFinished()).isFalse();

            booking.setStatus(BookingStatus.CONFIRMED);
            assertThat(booking.isFinished()).isFalse();
        }
    }
}
