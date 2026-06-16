package org.example.repository;

import org.example.domain.Payment;
import org.example.domain.PaymentMethod;
import org.example.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
  "spring.flyway.enabled=false",
  "spring.jpa.hibernate.ddl-auto=create-drop",
  "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
})
class PaymentRepositoryTest {

  @Autowired
  private PaymentRepository paymentRepository;

  private Payment savedPayment;

  @BeforeEach
  void setUp() {
    paymentRepository.deleteAll();
    savedPayment = paymentRepository.save(createPayment(1L, PaymentStatus.CREATED, "key-1"));
  }

  private Payment createPayment(Long bookingId, PaymentStatus status, String key) {
    Payment p = new Payment();
    p.setBookingId(bookingId);
    p.setAmount(BigDecimal.valueOf(500));
    p.setMethod(PaymentMethod.CARD);
    p.setCurrency("UAH");
    p.setStatus(status);
    p.setPaymentDate(LocalDateTime.now());
    p.setIdempotencyKey(key);
    p.setCreatedAt(LocalDateTime.now());
    p.setUpdatedAt(LocalDateTime.now());
    return p;
  }

  @Test
  void save_shouldPersistPayment() {
    assertThat(savedPayment.getId()).isNotNull();
  }

  @Test
  void findById_shouldReturnPayment() {
    var found = paymentRepository.findById(savedPayment.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getBookingId()).isEqualTo(1L);
  }

  @Test
  void findById_notFound_shouldReturnEmpty() {
    var found = paymentRepository.findById(999L);
    assertThat(found).isEmpty();
  }

  @Test
  void findByBookingId_shouldReturnPayments() {
    paymentRepository.save(createPayment(1L, PaymentStatus.PENDING, "key-2"));
    List<Payment> result = paymentRepository.findByBookingId(1L);
    assertThat(result).hasSize(2);
  }

  @Test
  void findByBookingId_differentBooking_shouldReturnEmpty() {
    List<Payment> result = paymentRepository.findByBookingId(999L);
    assertThat(result).isEmpty();
  }

  @Test
  void findByStatus_created_shouldReturnPayments() {
    List<Payment> result = paymentRepository.findByStatus(PaymentStatus.CREATED);
    assertThat(result).hasSize(1);
  }

  @Test
  void findByStatus_pending_shouldReturnEmpty() {
    List<Payment> result = paymentRepository.findByStatus(PaymentStatus.PENDING);
    assertThat(result).isEmpty();
  }

  @Test
  void findAll_shouldReturnAll() {
    paymentRepository.save(createPayment(2L, PaymentStatus.SUCCESS, "key-3"));
    assertThat(paymentRepository.findAll()).hasSize(2);
  }

  @Test
  void delete_shouldRemovePayment() {
    paymentRepository.delete(savedPayment);
    assertThat(paymentRepository.findById(savedPayment.getId())).isEmpty();
  }

  @Test
  void idempotencyKey_shouldBeUnique() {
    Payment duplicate = createPayment(2L, PaymentStatus.CREATED, "key-1");
    assertThat(org.junit.jupiter.api.Assertions.assertThrows(
      Exception.class,
      () -> paymentRepository.saveAndFlush(duplicate)
    )).isNotNull();
  }
}
