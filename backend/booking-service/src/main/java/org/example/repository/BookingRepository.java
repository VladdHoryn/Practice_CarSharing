package org.example.repository;

import java.util.List;
import org.example.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    public List<Booking> findByUserId(Long userId);
}
