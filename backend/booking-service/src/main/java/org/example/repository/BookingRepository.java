package org.example.repository;

import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

  List<Booking> findByUserId(Long userId);

  @Query(
    "SELECT COUNT(b) > 0 FROM Booking b WHERE b.carId = :carId "
      + "AND b.status != 'CANCELLED' "
      + "AND b.startDate < :endDate AND b.endDate > :startDate")
  boolean isCarAlreadyBooked(
    @Param("carId") Long carId,
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate);

    // =====================================================
    // OWNER ANALYTICS
    // =====================================================

    /**
     * 1) Загальна кількість бронювань, які належать певному OWNER
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :ownerId")
    long countBookingsByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 2) Кількість бронювань зі статусом COMPLETED, які належать певному OWNER
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :ownerId AND b.status = :status")
    long countCompletedBookingsByOwnerId(
        @Param("ownerId") Long ownerId,
        @Param("status") BookingStatus status
    );

    /**
     * 3) Загальна виручка за всі COMPLETED бронювання власника
     */
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.userId = :ownerId AND b.status = :status")
    double sumTotalPriceByOwnerIdAndStatus(
        @Param("ownerId") Long ownerId,
        @Param("status") BookingStatus status
    );

    /**
     * 5) Виручка за останні 12 місяців для OWNER (помісячно)
     */
    @Query("""
        SELECT FUNCTION('DATE_TRUNC', 'month', b.endDate) as month,
               COALESCE(SUM(b.totalPrice), 0) as revenue
        FROM Booking b
        WHERE b.userId = :ownerId
          AND b.status = :status
          AND b.endDate >= :startDate
        GROUP BY FUNCTION('DATE_TRUNC', 'month', b.endDate)
        ORDER BY month ASC
        """)
    List<Object[]> findMonthlyRevenueByOwnerId(
        @Param("ownerId") Long ownerId,
        @Param("status") BookingStatus status,
        @Param("startDate") LocalDateTime startDate
    );

    /**
     * 6) Кількість авто, заброньованих на кожен день наступних 7 днів
     */
    @Query("""
        SELECT FUNCTION('DATE', b.startDate) as date, COUNT(DISTINCT b.carId) as bookedCars
        FROM Booking b
        WHERE b.userId = :ownerId
          AND b.status IN :activeStatuses
          AND b.startDate <= :endDate
          AND b.endDate >= :startDate
        GROUP BY FUNCTION('DATE', b.startDate)
        ORDER BY date ASC
        """)
    List<Object[]> countBookedCarsByDayForOwner(
        @Param("ownerId") Long ownerId,
        @Param("activeStatuses") List<BookingStatus> activeStatuses,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
