package org.example.repository;

import org.example.domain.Car;
import org.example.domain.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {

    List<Car> findByStatus(CarStatus status);

    /**
     * 4) Кількість авто, які належать певному OWNER
     */
    @Query("SELECT COUNT(c) FROM Car c WHERE c.userId = :ownerId")
    long countByOwnerId(@Param("ownerId") Long ownerId);
}
