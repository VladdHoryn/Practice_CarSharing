package org.example.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.example.dto.AdminAnalyticsSummaryResponse;
import org.example.dto.OwnerAnalyticsSummaryResponse;
import org.example.infrastructure.client.BookingServiceClient;
import org.example.infrastructure.client.CarServiceClient;
import org.example.infrastructure.client.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AnalyticsAggregatorApplicationServiceTest {

    private static final String TOKEN = "Bearer test-token";
    private static final Long OWNER_ID = 42L;
    private static final String COMPLETED_STATUS = "COMPLETED";
    private static final List<String> ACTIVE_STATUSES = List.of("ACTIVE", "IN_PROGRESS");
    private static final List<String> ALL_STATUSES =
            List.of("PENDING", "CONFIRMED", "COMPLETED", "CANCELED");
    private static final LocalDateTime YEAR_START = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime WEEK_START = LocalDateTime.of(2026, 6, 15, 0, 0);
    private static final LocalDateTime WEEK_END = LocalDateTime.of(2026, 6, 21, 23, 59);
    private static final LocalDateTime PERIOD_START = LocalDateTime.of(2026, 5, 20, 0, 0);
    private static final LocalDateTime UPCOMING_START = LocalDateTime.of(2026, 6, 20, 0, 0);
    private static final LocalDateTime UPCOMING_END = LocalDateTime.of(2026, 7, 20, 0, 0);

    @Mock private CarServiceClient carServiceClient;
    @Mock private BookingServiceClient bookingServiceClient;
    @Mock private UserServiceClient userServiceClient;

    @InjectMocks private AnalyticsAggregatorApplicationService service;

    @BeforeEach
    void setUp() {
        lenient()
                .when(carServiceClient.countCarsByOwnerId(anyString(), anyLong()))
                .thenReturn(ResponseEntity.ok(0L));
        lenient()
                .when(bookingServiceClient.countBookingsByOwnerId(anyString(), anyLong()))
                .thenReturn(ResponseEntity.ok(0L));
        lenient()
                .when(
                        bookingServiceClient.countCompletedBookingsByOwnerId(
                                anyString(), anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(0L));
        lenient()
                .when(
                        bookingServiceClient.sumTotalPriceByOwnerIdAndStatus(
                                anyString(), anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(BigDecimal.ZERO));
        lenient()
                .when(
                        bookingServiceClient.findMonthlyRevenueByOwnerId(
                                anyString(), anyLong(), anyString(), any()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));
        lenient()
                .when(
                        bookingServiceClient.countBookedCarsByDayForOwner(
                                anyString(), anyLong(), anyList(), any(), any()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        lenient()
                .when(userServiceClient.countActiveUsers(anyString()))
                .thenReturn(ResponseEntity.ok(0L));
        lenient()
                .when(userServiceClient.countUsersByRole(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(0L));
        lenient()
                .when(bookingServiceClient.countBookingsByStatuses(anyString(), anyList()))
                .thenReturn(ResponseEntity.ok(0L));
        lenient()
                .when(bookingServiceClient.sumLastMonthRevenue(anyString(), anyString(), any()))
                .thenReturn(ResponseEntity.ok(BigDecimal.ZERO));
        lenient()
                .when(
                        bookingServiceClient.countUpcomingBookings(
                                anyString(), anyList(), any(), any()))
                .thenReturn(ResponseEntity.ok(0L));
        lenient()
                .when(bookingServiceClient.findMonthlyRevenue(anyString(), anyString(), any()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));
        lenient()
                .when(bookingServiceClient.countBookingsByDayOfWeek(anyString(), anyList()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));
    }

    @Nested
    @DisplayName("getOwnerAnalyticsSummary")
    class GetOwnerAnalyticsSummary {

        @Test
        @DisplayName("happy path: усі поля коректно змаплені з відповідей клієнтів")
        void returnsFullyPopulatedSummary_whenAllClientsSucceed() {
            List<Object[]> monthlyRevenue =
                    List.<Object[]>of(new Object[] {1, BigDecimal.valueOf(500)});
            List<Object[]> weeklyLoad = List.<Object[]>of(new Object[] {"MONDAY", 3L});

            when(carServiceClient.countCarsByOwnerId(TOKEN, OWNER_ID))
                    .thenReturn(ResponseEntity.ok(5L));
            when(bookingServiceClient.countBookingsByOwnerId(TOKEN, OWNER_ID))
                    .thenReturn(ResponseEntity.ok(20L));
            when(bookingServiceClient.countCompletedBookingsByOwnerId(
                            TOKEN, OWNER_ID, COMPLETED_STATUS))
                    .thenReturn(ResponseEntity.ok(15L));
            when(bookingServiceClient.sumTotalPriceByOwnerIdAndStatus(
                            TOKEN, OWNER_ID, COMPLETED_STATUS))
                    .thenReturn(ResponseEntity.ok(BigDecimal.valueOf(12345.67)));
            when(bookingServiceClient.findMonthlyRevenueByOwnerId(
                            TOKEN, OWNER_ID, COMPLETED_STATUS, YEAR_START))
                    .thenReturn(ResponseEntity.ok(monthlyRevenue));
            when(bookingServiceClient.countBookedCarsByDayForOwner(
                            TOKEN, OWNER_ID, ACTIVE_STATUSES, WEEK_START, WEEK_END))
                    .thenReturn(ResponseEntity.ok(weeklyLoad));

            OwnerAnalyticsSummaryResponse result =
                    service.getOwnerAnalyticsSummary(
                            TOKEN,
                            OWNER_ID,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            YEAR_START,
                            WEEK_START,
                            WEEK_END);

            assertThat(result.getTotalCars()).isEqualTo(5L);
            assertThat(result.getTotalBookings()).isEqualTo(20L);
            assertThat(result.getCompletedBookings()).isEqualTo(15L);
            assertThat(result.getTotalRevenue()).isEqualTo(BigDecimal.valueOf(12345.67));
            assertThat(result.getMonthlyRevenue()).isEqualTo(monthlyRevenue);
            assertThat(result.getWeeklyLoad()).isEqualTo(weeklyLoad);
        }

        @Test
        @DisplayName("fallback: countCarsByOwnerId кидає виняток -> totalCars = 0")
        void fallsBackToZero_whenCarServiceThrows() {
            when(carServiceClient.countCarsByOwnerId(TOKEN, OWNER_ID))
                    .thenThrow(new RuntimeException("car-service unavailable"));

            OwnerAnalyticsSummaryResponse result =
                    service.getOwnerAnalyticsSummary(
                            TOKEN,
                            OWNER_ID,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            YEAR_START,
                            WEEK_START,
                            WEEK_END);

            assertThat(result.getTotalCars()).isZero();
            assertThat(result.getTotalBookings()).isZero();
        }

        @Test
        @DisplayName("fallback: countBookingsByOwnerId кидає виняток -> totalBookings = 0")
        void fallsBackToZero_whenBookingCountThrows() {
            when(bookingServiceClient.countBookingsByOwnerId(TOKEN, OWNER_ID))
                    .thenThrow(new RuntimeException("booking-service down"));

            OwnerAnalyticsSummaryResponse result =
                    service.getOwnerAnalyticsSummary(
                            TOKEN,
                            OWNER_ID,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            YEAR_START,
                            WEEK_START,
                            WEEK_END);

            assertThat(result.getTotalBookings()).isZero();
        }

        @Test
        @DisplayName(
                "fallback: countCompletedBookingsByOwnerId кидає виняток -> completedBookings = 0")
        void fallsBackToZero_whenCompletedBookingsThrows() {
            when(bookingServiceClient.countCompletedBookingsByOwnerId(
                            TOKEN, OWNER_ID, COMPLETED_STATUS))
                    .thenThrow(new RuntimeException("timeout"));

            OwnerAnalyticsSummaryResponse result =
                    service.getOwnerAnalyticsSummary(
                            TOKEN,
                            OWNER_ID,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            YEAR_START,
                            WEEK_START,
                            WEEK_END);

            assertThat(result.getCompletedBookings()).isZero();
        }

        @Test
        @DisplayName(
                "fallback: sumTotalPriceByOwnerIdAndStatus кидає виняток -> totalRevenue = ZERO")
        void fallsBackToZeroRevenue_whenRevenueSumThrows() {
            when(bookingServiceClient.sumTotalPriceByOwnerIdAndStatus(
                            TOKEN, OWNER_ID, COMPLETED_STATUS))
                    .thenThrow(new RuntimeException("db error"));

            OwnerAnalyticsSummaryResponse result =
                    service.getOwnerAnalyticsSummary(
                            TOKEN,
                            OWNER_ID,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            YEAR_START,
                            WEEK_START,
                            WEEK_END);

            assertThat(result.getTotalRevenue()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("fallback: findMonthlyRevenueByOwnerId кидає виняток -> monthlyRevenue = null")
        void fallsBackToNull_whenMonthlyRevenueThrows() {
            when(bookingServiceClient.findMonthlyRevenueByOwnerId(
                            TOKEN, OWNER_ID, COMPLETED_STATUS, YEAR_START))
                    .thenThrow(new RuntimeException("feign error"));

            OwnerAnalyticsSummaryResponse result =
                    service.getOwnerAnalyticsSummary(
                            TOKEN,
                            OWNER_ID,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            YEAR_START,
                            WEEK_START,
                            WEEK_END);

            assertThat(result.getMonthlyRevenue()).isNull();
        }

        @Test
        @DisplayName("fallback: countBookedCarsByDayForOwner кидає виняток -> weeklyLoad = null")
        void fallsBackToNull_whenWeeklyLoadThrows() {
            when(bookingServiceClient.countBookedCarsByDayForOwner(
                            TOKEN, OWNER_ID, ACTIVE_STATUSES, WEEK_START, WEEK_END))
                    .thenThrow(new RuntimeException("feign error"));

            OwnerAnalyticsSummaryResponse result =
                    service.getOwnerAnalyticsSummary(
                            TOKEN,
                            OWNER_ID,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            YEAR_START,
                            WEEK_START,
                            WEEK_END);

            assertThat(result.getWeeklyLoad()).isNull();
        }

        @Test
        @DisplayName(
                "усі клієнти впали одночасно -> усі поля на дефолтах, виняток не пробивається назовні")
        void allFieldsFallBackToDefaults_whenAllClientsThrow() {
            when(carServiceClient.countCarsByOwnerId(TOKEN, OWNER_ID))
                    .thenThrow(new RuntimeException("fail"));
            when(bookingServiceClient.countBookingsByOwnerId(TOKEN, OWNER_ID))
                    .thenThrow(new RuntimeException("fail"));
            when(bookingServiceClient.countCompletedBookingsByOwnerId(
                            TOKEN, OWNER_ID, COMPLETED_STATUS))
                    .thenThrow(new RuntimeException("fail"));
            when(bookingServiceClient.sumTotalPriceByOwnerIdAndStatus(
                            TOKEN, OWNER_ID, COMPLETED_STATUS))
                    .thenThrow(new RuntimeException("fail"));
            when(bookingServiceClient.findMonthlyRevenueByOwnerId(
                            TOKEN, OWNER_ID, COMPLETED_STATUS, YEAR_START))
                    .thenThrow(new RuntimeException("fail"));
            when(bookingServiceClient.countBookedCarsByDayForOwner(
                            TOKEN, OWNER_ID, ACTIVE_STATUSES, WEEK_START, WEEK_END))
                    .thenThrow(new RuntimeException("fail"));

            OwnerAnalyticsSummaryResponse result =
                    service.getOwnerAnalyticsSummary(
                            TOKEN,
                            OWNER_ID,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            YEAR_START,
                            WEEK_START,
                            WEEK_END);

            assertThat(result.getTotalCars()).isZero();
            assertThat(result.getTotalBookings()).isZero();
            assertThat(result.getCompletedBookings()).isZero();
            assertThat(result.getTotalRevenue()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getMonthlyRevenue()).isNull();
            assertThat(result.getWeeklyLoad()).isNull();
        }

        @Test
        @DisplayName(
                "Feign повертає 200 з null тілом (не виняток) -> у DTO потрапляє null, без fallback")
        void propagatesNullBody_whenFeignReturnsEmptyBodyWithoutException() {
            when(carServiceClient.countCarsByOwnerId(TOKEN, OWNER_ID))
                    .thenReturn(ResponseEntity.ok(null));

            OwnerAnalyticsSummaryResponse result =
                    service.getOwnerAnalyticsSummary(
                            TOKEN,
                            OWNER_ID,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            YEAR_START,
                            WEEK_START,
                            WEEK_END);

            assertThat(result.getTotalCars()).isNull();
        }
    }

    @Nested
    @DisplayName("getAdminAnalyticsSummary")
    class GetAdminAnalyticsSummary {

        @Test
        @DisplayName("happy path: усі поля коректно змаплені з відповідей клієнтів")
        void returnsFullyPopulatedSummary_whenAllClientsSucceed() {
            List<Object[]> monthlyRevenue =
                    List.<Object[]>of(new Object[] {6, BigDecimal.valueOf(9999)});
            List<Object[]> dayOfWeekLoad = List.<Object[]>of(new Object[] {"FRIDAY", 12L});

            when(userServiceClient.countActiveUsers(TOKEN)).thenReturn(ResponseEntity.ok(100L));
            when(userServiceClient.countUsersByRole(TOKEN, "OWNER"))
                    .thenReturn(ResponseEntity.ok(30L));
            when(userServiceClient.countUsersByRole(TOKEN, "RENTER"))
                    .thenReturn(ResponseEntity.ok(70L));
            when(bookingServiceClient.countBookingsByStatuses(TOKEN, ALL_STATUSES))
                    .thenReturn(ResponseEntity.ok(500L));
            when(bookingServiceClient.sumLastMonthRevenue(TOKEN, COMPLETED_STATUS, PERIOD_START))
                    .thenReturn(ResponseEntity.ok(BigDecimal.valueOf(98765.43)));
            when(bookingServiceClient.countUpcomingBookings(
                            TOKEN, ACTIVE_STATUSES, UPCOMING_START, UPCOMING_END))
                    .thenReturn(ResponseEntity.ok(40L));
            when(bookingServiceClient.findMonthlyRevenue(TOKEN, COMPLETED_STATUS, YEAR_START))
                    .thenReturn(ResponseEntity.ok(monthlyRevenue));
            when(bookingServiceClient.countBookingsByDayOfWeek(TOKEN, ACTIVE_STATUSES))
                    .thenReturn(ResponseEntity.ok(dayOfWeekLoad));

            AdminAnalyticsSummaryResponse result =
                    service.getAdminAnalyticsSummary(
                            TOKEN,
                            ALL_STATUSES,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            PERIOD_START,
                            UPCOMING_START,
                            UPCOMING_END,
                            YEAR_START);

            assertThat(result.getActiveUsers()).isEqualTo(100L);
            assertThat(result.getTotalOwners()).isEqualTo(30L);
            assertThat(result.getTotalRenters()).isEqualTo(70L);
            assertThat(result.getTotalBookings()).isEqualTo(500L);
            assertThat(result.getPeriodRevenue()).isEqualTo(BigDecimal.valueOf(98765.43));
            assertThat(result.getUpcomingBookings()).isEqualTo(40L);
            assertThat(result.getMonthlyRevenue()).isEqualTo(monthlyRevenue);
            assertThat(result.getBookingsByDayOfWeek()).isEqualTo(dayOfWeekLoad);
        }

        @Test
        @DisplayName("fallback: countActiveUsers кидає виняток -> activeUsers = 0")
        void fallsBackToZero_whenActiveUsersThrows() {
            when(userServiceClient.countActiveUsers(TOKEN))
                    .thenThrow(new RuntimeException("user-service down"));

            AdminAnalyticsSummaryResponse result =
                    service.getAdminAnalyticsSummary(
                            TOKEN,
                            ALL_STATUSES,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            PERIOD_START,
                            UPCOMING_START,
                            UPCOMING_END,
                            YEAR_START);

            assertThat(result.getActiveUsers()).isZero();
        }

        @Test
        @DisplayName(
                "fallback: countUsersByRole(OWNER) кидає виняток -> totalOwners = 0, totalRenters рахується нормально")
        void fallsBackToZero_whenOwnersCountThrows() {
            when(userServiceClient.countUsersByRole(TOKEN, "OWNER"))
                    .thenThrow(new RuntimeException("fail"));
            when(userServiceClient.countUsersByRole(TOKEN, "RENTER"))
                    .thenReturn(ResponseEntity.ok(70L));

            AdminAnalyticsSummaryResponse result =
                    service.getAdminAnalyticsSummary(
                            TOKEN,
                            ALL_STATUSES,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            PERIOD_START,
                            UPCOMING_START,
                            UPCOMING_END,
                            YEAR_START);

            assertThat(result.getTotalOwners()).isZero();
            assertThat(result.getTotalRenters()).isEqualTo(70L);
        }

        @Test
        @DisplayName("fallback: countUsersByRole(RENTER) кидає виняток -> totalRenters = 0")
        void fallsBackToZero_whenRentersCountThrows() {
            when(userServiceClient.countUsersByRole(TOKEN, "RENTER"))
                    .thenThrow(new RuntimeException("fail"));

            AdminAnalyticsSummaryResponse result =
                    service.getAdminAnalyticsSummary(
                            TOKEN,
                            ALL_STATUSES,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            PERIOD_START,
                            UPCOMING_START,
                            UPCOMING_END,
                            YEAR_START);

            assertThat(result.getTotalRenters()).isZero();
        }

        @Test
        @DisplayName("fallback: countBookingsByStatuses кидає виняток -> totalBookings = 0")
        void fallsBackToZero_whenTotalBookingsThrows() {
            when(bookingServiceClient.countBookingsByStatuses(TOKEN, ALL_STATUSES))
                    .thenThrow(new RuntimeException("fail"));

            AdminAnalyticsSummaryResponse result =
                    service.getAdminAnalyticsSummary(
                            TOKEN,
                            ALL_STATUSES,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            PERIOD_START,
                            UPCOMING_START,
                            UPCOMING_END,
                            YEAR_START);

            assertThat(result.getTotalBookings()).isZero();
        }

        @Test
        @DisplayName("fallback: sumLastMonthRevenue кидає виняток -> periodRevenue = ZERO")
        void fallsBackToZeroRevenue_whenPeriodRevenueThrows() {
            when(bookingServiceClient.sumLastMonthRevenue(TOKEN, COMPLETED_STATUS, PERIOD_START))
                    .thenThrow(new RuntimeException("fail"));

            AdminAnalyticsSummaryResponse result =
                    service.getAdminAnalyticsSummary(
                            TOKEN,
                            ALL_STATUSES,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            PERIOD_START,
                            UPCOMING_START,
                            UPCOMING_END,
                            YEAR_START);

            assertThat(result.getPeriodRevenue()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("fallback: countUpcomingBookings кидає виняток -> upcomingBookings = 0")
        void fallsBackToZero_whenUpcomingBookingsThrows() {
            when(bookingServiceClient.countUpcomingBookings(
                            TOKEN, ACTIVE_STATUSES, UPCOMING_START, UPCOMING_END))
                    .thenThrow(new RuntimeException("fail"));

            AdminAnalyticsSummaryResponse result =
                    service.getAdminAnalyticsSummary(
                            TOKEN,
                            ALL_STATUSES,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            PERIOD_START,
                            UPCOMING_START,
                            UPCOMING_END,
                            YEAR_START);

            assertThat(result.getUpcomingBookings()).isZero();
        }

        @Test
        @DisplayName("fallback: findMonthlyRevenue кидає виняток -> monthlyRevenue = null")
        void fallsBackToNull_whenMonthlyRevenueThrows() {
            when(bookingServiceClient.findMonthlyRevenue(TOKEN, COMPLETED_STATUS, YEAR_START))
                    .thenThrow(new RuntimeException("fail"));

            AdminAnalyticsSummaryResponse result =
                    service.getAdminAnalyticsSummary(
                            TOKEN,
                            ALL_STATUSES,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            PERIOD_START,
                            UPCOMING_START,
                            UPCOMING_END,
                            YEAR_START);

            assertThat(result.getMonthlyRevenue()).isNull();
        }

        @Test
        @DisplayName(
                "fallback: countBookingsByDayOfWeek кидає виняток -> bookingsByDayOfWeek = null")
        void fallsBackToNull_whenDayOfWeekLoadThrows() {
            when(bookingServiceClient.countBookingsByDayOfWeek(TOKEN, ACTIVE_STATUSES))
                    .thenThrow(new RuntimeException("fail"));

            AdminAnalyticsSummaryResponse result =
                    service.getAdminAnalyticsSummary(
                            TOKEN,
                            ALL_STATUSES,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            PERIOD_START,
                            UPCOMING_START,
                            UPCOMING_END,
                            YEAR_START);

            assertThat(result.getBookingsByDayOfWeek()).isNull();
        }

        @Test
        @DisplayName(
                "усі клієнти впали одночасно -> усі поля на дефолтах, виняток не пробивається назовні")
        void allFieldsFallBackToDefaults_whenAllClientsThrow() {
            when(userServiceClient.countActiveUsers(TOKEN)).thenThrow(new RuntimeException("fail"));
            when(userServiceClient.countUsersByRole(eq(TOKEN), anyString()))
                    .thenThrow(new RuntimeException("fail"));
            when(bookingServiceClient.countBookingsByStatuses(TOKEN, ALL_STATUSES))
                    .thenThrow(new RuntimeException("fail"));
            when(bookingServiceClient.sumLastMonthRevenue(TOKEN, COMPLETED_STATUS, PERIOD_START))
                    .thenThrow(new RuntimeException("fail"));
            when(bookingServiceClient.countUpcomingBookings(
                            TOKEN, ACTIVE_STATUSES, UPCOMING_START, UPCOMING_END))
                    .thenThrow(new RuntimeException("fail"));
            when(bookingServiceClient.findMonthlyRevenue(TOKEN, COMPLETED_STATUS, YEAR_START))
                    .thenThrow(new RuntimeException("fail"));
            when(bookingServiceClient.countBookingsByDayOfWeek(TOKEN, ACTIVE_STATUSES))
                    .thenThrow(new RuntimeException("fail"));

            AdminAnalyticsSummaryResponse result =
                    service.getAdminAnalyticsSummary(
                            TOKEN,
                            ALL_STATUSES,
                            COMPLETED_STATUS,
                            ACTIVE_STATUSES,
                            PERIOD_START,
                            UPCOMING_START,
                            UPCOMING_END,
                            YEAR_START);

            assertThat(result.getActiveUsers()).isZero();
            assertThat(result.getTotalOwners()).isZero();
            assertThat(result.getTotalRenters()).isZero();
            assertThat(result.getTotalBookings()).isZero();
            assertThat(result.getPeriodRevenue()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getUpcomingBookings()).isZero();
            assertThat(result.getMonthlyRevenue()).isNull();
            assertThat(result.getBookingsByDayOfWeek()).isNull();
        }
    }
}
