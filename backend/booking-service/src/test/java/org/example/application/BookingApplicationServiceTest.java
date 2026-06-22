package org.example.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.example.infrastructure.client.CarServiceClient;
import org.example.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BookingApplicationServiceTest {

    @Mock private BookingRepository bookingRepository;

    @Mock private CarServiceClient carServiceClient;

    @InjectMocks private BookingApplicationService bookingService;

    private Booking pendingBooking;
    private Booking cancelledBooking;
    private Booking completedBooking;

    @BeforeEach
    void setUp() {
        pendingBooking = new Booking();
        pendingBooking.setId(1L);
        pendingBooking.setUserId(10L);
        pendingBooking.setCarId(20L);
        pendingBooking.setStatus(BookingStatus.PENDING);
        pendingBooking.setTotalPrice(BigDecimal.valueOf(300));
        pendingBooking.setStartDate(LocalDateTime.now().plusDays(5));
        pendingBooking.setEndDate(LocalDateTime.now().plusDays(8));
        pendingBooking.setCancelDeadline(LocalDateTime.now().plusDays(3));

        cancelledBooking = new Booking();
        cancelledBooking.setId(2L);
        cancelledBooking.setStatus(BookingStatus.CANCELLED);
        cancelledBooking.setTotalPrice(BigDecimal.valueOf(100));
        cancelledBooking.setStartDate(LocalDateTime.now().plusDays(1));
        cancelledBooking.setEndDate(LocalDateTime.now().plusDays(3));
        cancelledBooking.setCancelDeadline(LocalDateTime.now().minusDays(1));

        completedBooking = new Booking();
        completedBooking.setId(3L);
        completedBooking.setStatus(BookingStatus.COMPLETED);
        completedBooking.setTotalPrice(BigDecimal.valueOf(500));
        completedBooking.setStartDate(LocalDateTime.now().minusDays(5));
        completedBooking.setEndDate(LocalDateTime.now().minusDays(2));
        completedBooking.setCancelDeadline(LocalDateTime.now().minusDays(7));
    }

    @Nested
    @DisplayName("getBookingById()")
    class GetBookingByIdTests {

        @Test
        void shouldReturnBookingWhenFound() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
            Booking result = bookingService.getBookingById(1L);
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenNotFound() {
            when(bookingRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> bookingService.getBookingById(99L));
        }
    }

    @Nested
    @DisplayName("getAllBookings()")
    class GetAllBookingsTests {

        @Test
        void shouldReturnAllBookings() {
            when(bookingRepository.findAll()).thenReturn(List.of(pendingBooking, completedBooking));
            List<Booking> result = bookingService.getAllBookings();
            assertThat(result).hasSize(2);
        }

        @Test
        void shouldReturnEmptyListWhenNoBookings() {
            when(bookingRepository.findAll()).thenReturn(Collections.emptyList());
            assertThat(bookingService.getAllBookings()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserBookings()")
    class GetUserBookingsTests {

        @Test
        void shouldReturnUserBookings() {
            when(bookingRepository.findByUserId(10L)).thenReturn(List.of(pendingBooking));
            List<Booking> result = bookingService.getUserBookings(10L);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(10L);
        }

        @Test
        void shouldReturnEmptyForUserWithNoBookings() {
            when(bookingRepository.findByUserId(99L)).thenReturn(Collections.emptyList());
            assertThat(bookingService.getUserBookings(99L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("createBooking()")
    class CreateBookingTests {

        @Test
        void shouldCreateBookingSuccessfully() {
            when(bookingRepository.isCarAlreadyBooked(any(), any(), any())).thenReturn(false);
            when(bookingRepository.save(any(Booking.class))).thenReturn(pendingBooking);

            LocalDateTime now = LocalDateTime.now();
            Booking created =
                    bookingService.createBooking(
                            10L, 20L, now.plusDays(1), now.plusDays(3), BigDecimal.valueOf(100));

            assertNotNull(created);
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        void shouldThrowWhenCarAlreadyBooked() {
            when(bookingRepository.isCarAlreadyBooked(any(), any(), any())).thenReturn(true);

            LocalDateTime now = LocalDateTime.now();
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            bookingService.createBooking(
                                    10L,
                                    20L,
                                    now.plusDays(1),
                                    now.plusDays(3),
                                    BigDecimal.valueOf(100)));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        void shouldSetCreatedAtOnNewBooking() {
            when(bookingRepository.isCarAlreadyBooked(any(), any(), any())).thenReturn(false);
            when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LocalDateTime now = LocalDateTime.now();
            Booking created =
                    bookingService.createBooking(
                            10L, 20L, now.plusDays(1), now.plusDays(3), BigDecimal.valueOf(100));

            assertThat(created.getCreatedAt()).isNotNull();
        }

        @Test
        void shouldSetCancelDeadlineBasedOnStartDate() {
            when(bookingRepository.isCarAlreadyBooked(any(), any(), any())).thenReturn(false);
            when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LocalDateTime startDate = LocalDateTime.now().plusDays(10);
            Booking created =
                    bookingService.createBooking(
                            10L, 20L, startDate, startDate.plusDays(3), BigDecimal.valueOf(100));

            assertThat(created.getCancelDeadline()).isEqualTo(startDate.minusDays(2));
        }
    }

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatusTests {

        @Test
        void shouldChangeStatusSuccessfully() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
            bookingService.changeStatus(1L, BookingStatus.CONFIRMED);
            assertThat(pendingBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        void shouldThrowWhenBookingNotFound() {
            when(bookingRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(
                    IllegalArgumentException.class,
                    () -> bookingService.changeStatus(99L, BookingStatus.CONFIRMED));
        }
    }

    @Nested
    @DisplayName("cancelBooking()")
    class CancelBookingTests {

        @Test
        void shouldCancelBookingBeforeDeadline() {
            pendingBooking.setCancelDeadline(LocalDateTime.now().plusDays(2));
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));

            Booking cancelled = bookingService.cancelBooking(1L);
            assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }

        @Test
        void shouldThrowWhenCancellingCompletedBooking() {
            completedBooking.setCancelDeadline(LocalDateTime.now().plusDays(1));
            when(bookingRepository.findById(3L)).thenReturn(Optional.of(completedBooking));

            assertThrows(IllegalStateException.class, () -> bookingService.cancelBooking(3L));
        }

        @Test
        void shouldThrowWhenDeadlineExpired() {
            pendingBooking.setCancelDeadline(LocalDateTime.now().minusDays(1));
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));

            assertThrows(IllegalStateException.class, () -> bookingService.cancelBooking(1L));
        }
    }

    @Nested
    @DisplayName("deleteBooking()")
    class DeleteBookingTests {

        @Test
        void shouldDeleteCancelledBookingSuccessfully() {
            when(bookingRepository.findById(2L)).thenReturn(Optional.of(cancelledBooking));
            doNothing().when(bookingRepository).delete(any());

            assertDoesNotThrow(() -> bookingService.deleteBooking(2L));
            verify(bookingRepository).delete(cancelledBooking);
        }

        @Test
        void shouldDeleteCompletedBookingSuccessfully() {
            when(bookingRepository.findById(3L)).thenReturn(Optional.of(completedBooking));
            doNothing().when(bookingRepository).delete(any());

            assertDoesNotThrow(() -> bookingService.deleteBooking(3L));
            verify(bookingRepository).delete(completedBooking);
        }

        @Test
        void shouldThrowWhenDeletingActiveBooking() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));

            assertThrows(IllegalStateException.class, () -> bookingService.deleteBooking(1L));
            verify(bookingRepository, never()).delete(any());
        }

        @Test
        void shouldThrowWhenBookingNotFound() {
            when(bookingRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RuntimeException.class, () -> bookingService.deleteBooking(99L));
        }
    }

    @Nested
    @DisplayName("countBookingsByStatuses()")
    class CountBookingsByStatusesTests {

        @Test
        void shouldReturnCountForGivenStatuses() {
            when(bookingRepository.countBookingsByStatuses(any())).thenReturn(5L);
            long count =
                    bookingService.countBookingsByStatuses(
                            List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING));
            assertThat(count).isEqualTo(5L);
        }

        @Test
        void shouldReturnZeroWhenStatusListIsEmpty() {
            long count = bookingService.countBookingsByStatuses(Collections.emptyList());
            assertThat(count).isZero();
            verify(bookingRepository, never()).countBookingsByStatuses(any());
        }

        @Test
        void shouldReturnZeroWhenStatusListIsNull() {
            long count = bookingService.countBookingsByStatuses(null);
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("sumLastMonthRevenue()")
    class SumLastMonthRevenueTests {

        @Test
        void shouldReturnRevenueSum() {
            when(bookingRepository.sumLastMonthRevenue(any(), any()))
                    .thenReturn(BigDecimal.valueOf(1500));

            BigDecimal result =
                    bookingService.sumLastMonthRevenue(
                            BookingStatus.COMPLETED, LocalDateTime.now().minusDays(30));
            assertThat(result).isEqualTo(BigDecimal.valueOf(1500));
        }
    }

    @Nested
    @DisplayName("countUpcomingBookings()")
    class CountUpcomingBookingsTests {

        @Test
        void shouldReturnUpcomingCount() {
            when(bookingRepository.countUpcomingBookings(any(), any(), any())).thenReturn(7L);
            long count =
                    bookingService.countUpcomingBookings(
                            List.of(BookingStatus.CONFIRMED),
                            LocalDateTime.now(),
                            LocalDateTime.now().plusDays(30));
            assertThat(count).isEqualTo(7L);
        }
    }
}
