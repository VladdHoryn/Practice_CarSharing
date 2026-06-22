package org.example.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BookingDriverTest {

    private BookingDriver driver;

    @BeforeEach
    void setUp() {
        driver = new BookingDriver();
        driver.setBookingId(1L);
        driver.setUserId(2L);
        driver.setEmail("driver@example.com");
        driver.setDriverCode("ABCD123456");
    }

    @Nested
    @DisplayName("onCreate() lifecycle hook")
    class OnCreateTests {

        @Test
        @DisplayName("встановлює createdAt і updatedAt")
        void setsTimestamps() {
            driver.onCreate();

            assertThat(driver.getCreatedAt()).isNotNull();
            assertThat(driver.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("встановлює статус PENDING, якщо статус не задано")
        void defaultsStatusToPending() {
            driver.setStatus(null);

            driver.onCreate();

            assertThat(driver.getStatus()).isEqualTo(BookingDriverStatus.PENDING);
        }

        @Test
        @DisplayName("не перезатирає статус, якщо він уже заданий")
        void doesNotOverrideExistingStatus() {
            driver.setStatus(BookingDriverStatus.ACCEPTED);

            driver.onCreate();

            assertThat(driver.getStatus()).isEqualTo(BookingDriverStatus.ACCEPTED);
        }
    }

    @Nested
    @DisplayName("accept() / decline()")
    class TransitionTests {

        @Test
        @DisplayName("accept() встановлює статус ACCEPTED")
        void acceptSetsAccepted() {
            driver.setStatus(BookingDriverStatus.PENDING);

            driver.accept();

            assertThat(driver.getStatus()).isEqualTo(BookingDriverStatus.ACCEPTED);
        }

        @Test
        @DisplayName("decline() встановлює статус DECLINED")
        void declineSetsDeclined() {
            driver.setStatus(BookingDriverStatus.PENDING);

            driver.decline();

            assertThat(driver.getStatus()).isEqualTo(BookingDriverStatus.DECLINED);
        }

        @Test
        @DisplayName(
                "accept() можна викликати навіть з нетипового стану (без захисту в самому entity)")
        void acceptDoesNotGuardPreviousState() {
            driver.setStatus(BookingDriverStatus.DECLINED);

            driver.accept();

            assertThat(driver.getStatus()).isEqualTo(BookingDriverStatus.ACCEPTED);
        }
    }

    @Nested
    @DisplayName("isPending() / isAccepted() / isDeclined()")
    class StatusPredicateTests {

        @Test
        @DisplayName("isPending() true лише для PENDING")
        void isPendingOnlyForPending() {
            driver.setStatus(BookingDriverStatus.PENDING);
            assertThat(driver.isPending()).isTrue();
            assertThat(driver.isAccepted()).isFalse();
            assertThat(driver.isDeclined()).isFalse();
        }

        @Test
        @DisplayName("isAccepted() true лише для ACCEPTED")
        void isAcceptedOnlyForAccepted() {
            driver.setStatus(BookingDriverStatus.ACCEPTED);
            assertThat(driver.isAccepted()).isTrue();
            assertThat(driver.isPending()).isFalse();
            assertThat(driver.isDeclined()).isFalse();
        }

        @Test
        @DisplayName("isDeclined() true лише для DECLINED")
        void isDeclinedOnlyForDeclined() {
            driver.setStatus(BookingDriverStatus.DECLINED);
            assertThat(driver.isDeclined()).isTrue();
            assertThat(driver.isPending()).isFalse();
            assertThat(driver.isAccepted()).isFalse();
        }
    }
}
