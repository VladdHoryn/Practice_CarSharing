package org.example.repository;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import org.example.domain.Car;
import org.example.domain.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {

    public List<Car> findByStatus(@NotNull(message = "Car status is required") CarStatus status);

    public List<Car> findCarByUserId(
            @NotNull(message = "Owner does not have any cars") Long userId);

    /** 4) Кількість авто, які належать певному OWNER */
    @Query("SELECT COUNT(c) FROM Car c WHERE c.userId = :ownerId")
    long countByOwnerId(@Param("ownerId") Long ownerId);
}
