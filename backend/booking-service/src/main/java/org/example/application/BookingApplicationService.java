package org.example.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.example.dto.CarDto;
import org.example.infrastructure.client.CarServiceClient;
import org.example.repository.BookingRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingApplicationService {

    private final BookingRepository bookingRepository;

    private final CarServiceClient carServiceClient;

    @Transactional
    public Booking createBooking(
            Long userId,
            Long carId,
            LocalDateTime start,
            LocalDateTime end,
            BigDecimal pricePerDay) {

        log.info("Creating booking: userId={}, carId={}", userId, carId);

        if (bookingRepository.isCarAlreadyBooked(carId, start, end)) {
            log.warn("Car {} is already booked for dates {} - {}", carId, start, end);
            throw new IllegalArgumentException(
                    "Цей автомобіль вже заброньовано на обрані дати. Будь ласка, оберіть інші дні.");
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setCarId(carId);
        booking.setStartDate(start);
        booking.setEndDate(end);

        booking.setStatus(BookingStatus.CREATED);

        booking.calculateTotalPrice(pricePerDay);

        booking.setCreatedAt(LocalDateTime.now());

        booking.setCancelDeadline(start.minusDays(2));

        Booking saved = bookingRepository.save(booking);

        return saved;
    }

    public Booking getBookingById(Long id) {
        return bookingRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found id=" + id));
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Transactional
    public void changeStatus(Long bookingId, BookingStatus newStatus) {
        Booking booking = getBookingById(bookingId);

        booking.changeStatus(newStatus);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        booking.cancel();

        return booking;
    }

    @Transactional
    public void deleteBooking(Long id) {
        log.info("Deleting booking id={}", id);

        Booking booking = getBookingById(id);

        if (!booking.isFinished()) {
            throw new IllegalStateException("Cannot delete active booking");
        }

        bookingRepository.delete(booking);
    }

    private List<Long> getCarIdsByOwner(Long ownerId) {
        log.debug("Fetching cars for ownerId={} from car-service", ownerId);
        List<CarDto> cars = carServiceClient.getCarsByUserId(ownerId);

        if (cars == null || cars.isEmpty()) {
            return Collections.emptyList();
        }

        return cars.stream().map(CarDto::id).toList();
    }

    public long countBookingsByOwnerId(Long ownerId) {
        List<Long> carIds = getCarIdsByOwner(ownerId);
        if (carIds.isEmpty()) return 0L;

        return bookingRepository.countBookingsByCarIds(carIds);
    }

    public long countCompletedBookingsByOwnerId(Long ownerId, BookingStatus status) {
        List<Long> carIds = getCarIdsByOwner(ownerId);
        if (carIds.isEmpty()) return 0L;

        return bookingRepository.countCompletedBookingsByCarIds(carIds, status);
    }

    public BigDecimal sumTotalPriceByOwnerIdAndStatus(Long ownerId, BookingStatus status) {
        List<Long> carIds = getCarIdsByOwner(ownerId);
        if (carIds.isEmpty()) return BigDecimal.ZERO;

        return bookingRepository.sumTotalPriceByCarIdsAndStatus(carIds, status);
    }

    public List<Object[]> findMonthlyRevenueByOwnerId(
            Long ownerId, BookingStatus status, LocalDateTime startDate) {
        List<Long> carIds = getCarIdsByOwner(ownerId);
        if (carIds.isEmpty()) return Collections.emptyList();

        return bookingRepository.findMonthlyRevenueByCarIds(carIds, status, startDate);
    }

    public List<Object[]> countBookedCarsByDayForOwner(
            Long ownerId,
            List<BookingStatus> activeStatuses,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        List<Long> carIds = getCarIdsByOwner(ownerId);
        if (carIds.isEmpty()) return Collections.emptyList();

        return bookingRepository.countBookedCarsByDayForCarIds(
                carIds, activeStatuses, startDate, endDate);
    }

    public long countBookingsByStatuses(List<BookingStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return 0L;
        }
        return bookingRepository.countBookingsByStatuses(statuses);
    }

    public BigDecimal sumLastMonthRevenue(BookingStatus status, LocalDateTime startDate) {
        return bookingRepository.sumLastMonthRevenue(status, startDate);
    }

    public long countUpcomingBookings(
            List<BookingStatus> activeStatuses, LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.countUpcomingBookings(activeStatuses, startDate, endDate);
    }

    public List<Object[]> findMonthlyRevenue(BookingStatus status, LocalDateTime startDate) {
        return bookingRepository.findMonthlyRevenue(status, startDate);
    }

    public List<Object[]> countBookingsByDayOfWeek(List<BookingStatus> activeStatuses) {
        return bookingRepository.countBookingsByDayOfWeek(activeStatuses);
    }

    public List<Booking> getActiveBookingsByCarIdFromToday(Long carId) {
        log.info("Fetching future active bookings for car id={}", carId);

        return bookingRepository.findAllByCarIdAndStatusNotAndEndDateAfterOrderByStartDateAsc(
                carId, BookingStatus.CANCELLED, LocalDateTime.now());
    }

    public List<Long> getAvailableCarIds(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Searching for available cars from {} to {}", startDate, endDate);

        LocalDateTime now = LocalDateTime.now();

        if (startDate.isBefore(now)) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new IllegalArgumentException("End date must be strictly after start date");
        }

        List<Long> allCarIds = carServiceClient.getCarIds();
        if (allCarIds == null || allCarIds.isEmpty()) {
            return List.of();
        }

        List<Long> bookedCarIds = bookingRepository.findBookedCarIdsForPeriod(startDate, endDate);

        Set<Long> bookedCarIdsSet = Set.copyOf(bookedCarIds);

        return allCarIds.stream().filter(carId -> !bookedCarIdsSet.contains(carId)).toList();
    }
}
