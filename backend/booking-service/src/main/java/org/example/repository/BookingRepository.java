package org.example.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.example.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

  List<Booking> findByUserId(Long userId);

  // Видалили 'FAILED', залишили тільки 'CANCELLED'
  @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.carId = :carId " +
    "AND b.status != 'CANCELLED' " +
    "AND b.startDate < :endDate AND b.endDate > :startDate")
  boolean isCarAlreadyBooked(@Param("carId") Long carId,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);
}
