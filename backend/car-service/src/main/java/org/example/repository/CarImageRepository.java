package org.example.repository;

import java.util.List;
import java.util.Optional;

import org.example.domain.CarImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarImageRepository extends JpaRepository<CarImage, Long> {

    List<CarImage> findByCarId(Long carId);

    Optional<CarImage> findByCarIdAndIsMainTrue(Long carId);

    @Query("SELECT ci FROM CarImage ci WHERE ci.car.id = :carId AND ci.isMain = true")
    Optional<CarImage> findMainImageByCarId(@Param("carId") Long carId);

    void deleteByCarId(Long carId);

    @Modifying
    @Query("UPDATE CarImage ci SET ci.isMain = false WHERE ci.car.id = :carId")
    void clearMainFlagForCar(@Param("carId") Long carId);

    List<CarImage> findByCarIdInAndIsMainTrue(List<Long> carIds);
}
