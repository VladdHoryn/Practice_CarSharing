package org.example.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.example.domain.BookingDriver;
import org.example.domain.BookingDriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingDriverRepository extends JpaRepository<BookingDriver, Long> {
    long countByBookingIdAndStatusIn(Long bookingId, Collection<BookingDriverStatus> statuses);

    boolean existsByBookingIdAndUserId(Long bookingId, Long userId);

    Optional<List<BookingDriver>> findByUserId(Long userId);

    List<BookingDriver> findAllByBookingIdAndStatusIn(Long bookingId, List<BookingDriverStatus> statuses);
}
