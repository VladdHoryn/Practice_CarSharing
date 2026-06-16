package org.example.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    // 1. Кількість бронювань для списку авто
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.carId IN :carIds")
    long countBookingsByCarIds(@Param("carIds") List<Long> carIds);

    // 2. Кількість виконаних (чи інших) бронювань для списку авто
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.carId IN :carIds AND b.status = :status")
    long countCompletedBookingsByCarIds(
            @Param("carIds") List<Long> carIds, @Param("status") BookingStatus status);

    // 3. Загальний дохід (сума) для списку авто
    @Query(
            "SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.carId IN :carIds AND b.status = :status")
    BigDecimal sumTotalPriceByCarIdsAndStatus(
            @Param("carIds") List<Long> carIds, @Param("status") BookingStatus status);

    // 4. Дохід по місяцях
    @Query(
            "SELECT EXTRACT(MONTH FROM b.startDate) as month, SUM(b.totalPrice) as revenue "
                    + "FROM Booking b WHERE b.carId IN :carIds AND b.status = :status AND b.startDate >= :startDate "
                    + "GROUP BY EXTRACT(MONTH FROM b.startDate)")
    List<Object[]> findMonthlyRevenueByCarIds(
            @Param("carIds") List<Long> carIds,
            @Param("status") BookingStatus status,
            @Param("startDate") LocalDateTime startDate);

    // 5. Завантаженість по днях (кількість заброньованих авто)
    @Query(
            "SELECT CAST(b.startDate AS date) as date, COUNT(b.carId) as count "
                    + "FROM Booking b WHERE b.carId IN :carIds AND b.status IN :activeStatuses "
                    + "AND b.startDate >= :startDate AND b.endDate <= :endDate "
                    + "GROUP BY CAST(b.startDate AS date)")
    List<Object[]> countBookedCarsByDayForCarIds(
            @Param("carIds") List<Long> carIds,
            @Param("activeStatuses") List<BookingStatus> activeStatuses,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
