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

    // =====================================================
    // ADMIN ANALYTICS
    // =====================================================

    // 1) Загальна кількість бронювань в системі за вказаним списком статусів
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status IN :statuses")
    long countBookingsByStatuses(@Param("statuses") List<BookingStatus> statuses);

    // 2) Дохід за останній місяць (бронювання, завершені за останні 30 днів)
    @Query(
            "SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = :status AND b.endDate >= :startDate")
    BigDecimal sumLastMonthRevenue(
            @Param("status") BookingStatus status, @Param("startDate") LocalDateTime startDate);

    // 3) Кількість бронювань в процесі протягом наступних 7 днів
    @Query(
            """
        SELECT COUNT(DISTINCT b) FROM Booking b
        WHERE b.status IN :activeStatuses
          AND b.startDate <= :endDate
          AND b.endDate >= :startDate
        """)
    long countUpcomingBookings(
            @Param("activeStatuses") List<BookingStatus> activeStatuses,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 4) Динаміка доходів за останні N місяців (для графіка)
    @Query(
            """
        SELECT EXTRACT(YEAR FROM b.endDate) as year, EXTRACT(MONTH FROM b.endDate) as month,
               COALESCE(SUM(b.totalPrice), 0) as revenue
        FROM Booking b
        WHERE b.status = :status
          AND b.endDate >= :startDate
        GROUP BY EXTRACT(YEAR FROM b.endDate), EXTRACT(MONTH FROM b.endDate)
        ORDER BY EXTRACT(YEAR FROM b.endDate) ASC, EXTRACT(MONTH FROM b.endDate) ASC
        """)
    List<Object[]> findMonthlyRevenue(
            @Param("status") BookingStatus status, @Param("startDate") LocalDateTime startDate);

    // 5) Завантаженість автопарку по днях тижня (бронювання/день)
    @Query(
            """
        SELECT FUNCTION('date_part', 'isodow', b.startDate) as dayOfWeek, COUNT(b) as count
        FROM Booking b
        WHERE b.status IN :activeStatuses
    GROUP BY FUNCTION('date_part', 'isodow', b.startDate)
   ORDER BY FUNCTION('date_part', 'isodow', b.startDate) ASC
        """)
    List<Object[]> countBookingsByDayOfWeek(
            @Param("activeStatuses") List<BookingStatus> activeStatuses);
}
