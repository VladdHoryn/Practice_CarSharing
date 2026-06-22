package org.example.application;

import org.example.domain.BookingDriver;
import org.example.domain.BookingDriverStatus;
import org.example.repository.BookingDriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingDriverApplicationServiceTest {

  @Mock
  private BookingDriverRepository bookingDriverRepository;

  @InjectMocks
  private BookingDriverApplicationService bookingDriverService;

  private BookingDriver pendingDriver;
  private BookingDriver acceptedDriver;

  @BeforeEach
  void setUp() {
    pendingDriver = new BookingDriver();
    pendingDriver.setId(1L);
    pendingDriver.setBookingId(10L);
    pendingDriver.setUserId(20L);
    pendingDriver.setEmail("driver@example.com");
    pendingDriver.setDriverCode("ABCD123456");
    pendingDriver.setStatus(BookingDriverStatus.PENDING);

    acceptedDriver = new BookingDriver();
    acceptedDriver.setId(2L);
    acceptedDriver.setBookingId(10L);
    acceptedDriver.setUserId(30L);
    acceptedDriver.setEmail("driver2@example.com");
    acceptedDriver.setDriverCode("XXXX123456");
    acceptedDriver.setStatus(BookingDriverStatus.ACCEPTED);
  }


  @Nested
  @DisplayName("createInvitation()")
  class CreateInvitationTests {

    @Test
    @DisplayName("успішно створює запрошення")
    void shouldCreateInvitationSuccessfully() {
      when(bookingDriverRepository.existsByBookingIdAndUserId(10L, 20L)).thenReturn(false);
      when(bookingDriverRepository.countByBookingIdAndStatusIn(eq(10L), anyList())).thenReturn(0L);
      when(bookingDriverRepository.save(any())).thenReturn(pendingDriver);

      BookingDriver result = bookingDriverService.createInvitation(
        10L, 20L, "driver@example.com", "ABCD123456");

      assertThat(result).isNotNull();
      verify(bookingDriverRepository).save(any());
    }

    @Test
    @DisplayName("кидає IllegalStateException якщо водій вже запрошений")
    void shouldThrowWhenUserAlreadyInvited() {
      when(bookingDriverRepository.existsByBookingIdAndUserId(10L, 20L)).thenReturn(true);

      assertThatThrownBy(() -> bookingDriverService.createInvitation(
        10L, 20L, "driver@example.com", "ABCD123456"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already been invited");
      verify(bookingDriverRepository, never()).save(any());
    }

    @Test
    @DisplayName("кидає IllegalStateException якщо досягнуто максимум водіїв (2)")
    void shouldThrowWhenMaxDriversReached() {
      when(bookingDriverRepository.existsByBookingIdAndUserId(10L, 20L)).thenReturn(false);
      when(bookingDriverRepository.countByBookingIdAndStatusIn(eq(10L), anyList())).thenReturn(2L);

      assertThatThrownBy(() -> bookingDriverService.createInvitation(
        10L, 20L, "driver@example.com", "ABCD123456"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Maximum number of additional drivers");
      verify(bookingDriverRepository, never()).save(any());
    }

    @Test
    @DisplayName("дозволяє запросити другого водія (count=1)")
    void shouldAllowSecondDriver() {
      when(bookingDriverRepository.existsByBookingIdAndUserId(10L, 25L)).thenReturn(false);
      when(bookingDriverRepository.countByBookingIdAndStatusIn(eq(10L), anyList())).thenReturn(1L);
      when(bookingDriverRepository.save(any())).thenReturn(pendingDriver);

      BookingDriver result = bookingDriverService.createInvitation(
        10L, 25L, "another@example.com", "ZZZZ123456");
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("нове запрошення має статус PENDING")
    void shouldSetStatusToPending() {
      when(bookingDriverRepository.existsByBookingIdAndUserId(any(), any())).thenReturn(false);
      when(bookingDriverRepository.countByBookingIdAndStatusIn(any(), any())).thenReturn(0L);
      when(bookingDriverRepository.save(any())).thenAnswer(inv -> {
        BookingDriver bd = inv.getArgument(0);
        assertThat(bd.getStatus()).isEqualTo(BookingDriverStatus.PENDING);
        return bd;
      });

      bookingDriverService.createInvitation(10L, 20L, "x@x.com", "1234567890");
    }
  }


  @Nested
  @DisplayName("acceptInvitation()")
  class AcceptInvitationTests {

    @Test
    @DisplayName("успішно приймає PENDING запрошення")
    void shouldAcceptPendingInvitation() {
      when(bookingDriverRepository.findById(1L)).thenReturn(Optional.of(pendingDriver));
      when(bookingDriverRepository.save(any())).thenReturn(pendingDriver);

      BookingDriver result = bookingDriverService.acceptInvitation(1L);
      assertThat(result.getStatus()).isEqualTo(BookingDriverStatus.ACCEPTED);
    }

    @Test
    @DisplayName("кидає IllegalArgumentException якщо запрошення не знайдено")
    void shouldThrowWhenInvitationNotFound() {
      when(bookingDriverRepository.findById(99L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> bookingDriverService.acceptInvitation(99L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invitation not found");
    }

    @Test
    @DisplayName("кидає IllegalStateException якщо запрошення вже оброблено (ACCEPTED)")
    void shouldThrowWhenAlreadyAccepted() {
      when(bookingDriverRepository.findById(2L)).thenReturn(Optional.of(acceptedDriver));
      assertThatThrownBy(() -> bookingDriverService.acceptInvitation(2L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already been processed");
    }

    @Test
    @DisplayName("кидає IllegalStateException якщо запрошення відхилено (DECLINED)")
    void shouldThrowWhenDeclined() {
      acceptedDriver.setStatus(BookingDriverStatus.DECLINED);
      when(bookingDriverRepository.findById(2L)).thenReturn(Optional.of(acceptedDriver));
      assertThatThrownBy(() -> bookingDriverService.acceptInvitation(2L))
        .isInstanceOf(IllegalStateException.class);
    }
  }


  @Nested
  @DisplayName("declineInvitation()")
  class DeclineInvitationTests {

    @Test
    @DisplayName("успішно відхиляє PENDING запрошення")
    void shouldDeclinePendingInvitation() {
      when(bookingDriverRepository.findById(1L)).thenReturn(Optional.of(pendingDriver));
      when(bookingDriverRepository.save(any())).thenReturn(pendingDriver);

      BookingDriver result = bookingDriverService.declineInvitation(1L);
      assertThat(result.getStatus()).isEqualTo(BookingDriverStatus.DECLINED);
    }

    @Test
    @DisplayName("кидає IllegalArgumentException якщо запрошення не знайдено")
    void shouldThrowWhenInvitationNotFound() {
      when(bookingDriverRepository.findById(99L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> bookingDriverService.declineInvitation(99L))
        .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("кидає IllegalStateException якщо запрошення вже оброблено (ACCEPTED)")
    void shouldThrowWhenAlreadyAccepted() {
      when(bookingDriverRepository.findById(2L)).thenReturn(Optional.of(acceptedDriver));
      assertThatThrownBy(() -> bookingDriverService.declineInvitation(2L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already been processed");
    }
  }


  @Nested
  @DisplayName("getById()")
  class GetByIdTests {

    @Test
    @DisplayName("повертає запрошення за ID")
    void shouldReturnInvitationById() {
      when(bookingDriverRepository.findById(1L)).thenReturn(Optional.of(pendingDriver));
      BookingDriver result = bookingDriverService.getById(1L);
      assertThat(result).isEqualTo(pendingDriver);
    }

    @Test
    @DisplayName("кидає виняток якщо не знайдено")
    void shouldThrowWhenNotFound() {
      when(bookingDriverRepository.findById(99L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> bookingDriverService.getById(99L))
        .isInstanceOf(IllegalArgumentException.class);
    }
  }


  @Nested
  @DisplayName("getByUserId()")
  class GetByUserIdTests {

    @Test
    @DisplayName("повертає запрошення для користувача")
    void shouldReturnInvitationsForUser() {
      when(bookingDriverRepository.findByUserId(20L))
        .thenReturn(Optional.of(List.of(pendingDriver)));
      List<BookingDriver> result = bookingDriverService.getByUserId(20L);
      assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("кидає виняток якщо нічого не знайдено")
    void shouldThrowWhenNoInvitationsFound() {
      when(bookingDriverRepository.findByUserId(99L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> bookingDriverService.getByUserId(99L))
        .isInstanceOf(IllegalArgumentException.class);
    }
  }


  @Nested
  @DisplayName("getAll()")
  class GetAllTests {

    @Test
    @DisplayName("повертає всі запрошення")
    void shouldReturnAllInvitations() {
      when(bookingDriverRepository.findAll()).thenReturn(List.of(pendingDriver, acceptedDriver));
      List<BookingDriver> result = bookingDriverService.getAll();
      assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("повертає порожній список якщо немає запрошень")
    void shouldReturnEmptyList() {
      when(bookingDriverRepository.findAll()).thenReturn(List.of());
      assertThat(bookingDriverService.getAll()).isEmpty();
    }
  }
}
