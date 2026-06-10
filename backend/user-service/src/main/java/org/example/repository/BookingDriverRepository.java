package org.example.repository;

import org.example.domain.BookingDriver;
import org.example.domain.BookingDriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface BookingDriverRepository extends JpaRepository<BookingDriver, Long> {
  long countByBookingIdAndStatusIn(
    Long bookingId,
    Collection<BookingDriverStatus> statuses
  );
}
