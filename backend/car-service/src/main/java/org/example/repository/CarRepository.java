package org.example.repository;

import jakarta.validation.constraints.NotNull;
import org.example.domain.Car;
import org.example.domain.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
  public List<Car> findByStatus(@NotNull(message = "Car status is required") CarStatus status);
}
