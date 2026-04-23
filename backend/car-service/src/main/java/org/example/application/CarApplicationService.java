package org.example.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Car;
import org.example.domain.CarStatus;
import org.example.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarApplicationService {
  private final CarRepository carRepository;

  @Transactional
  public Car createCar(Car car) {
    log.info("Creating new car: brand={}, model={}", car.getBrand(), car.getModel());

    car.setStatus(CarStatus.AVAILABLE);

    return carRepository.save(car);
  }


  public Car getCarById(Long id) {
    return carRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Car not found with id: " + id));
  }

  public List<Car> getAllCars() {
    return carRepository.findAll();
  }

  public List<Car> getAvailableCars() {
    return carRepository.findByStatus(CarStatus.AVAILABLE);
  }

  @Transactional
  public Car rentCar(Long carId, Long userId) {
    log.info("Rent request: carId={}, userId={}", carId, userId);

    Car car = getCarById(carId);

    car.rent(userId);

    return carRepository.save(car);
  }

  @Transactional
  public Car returnCar(Long carId) {
    log.info("Return request: carId={}", carId);

    Car car = getCarById(carId);

    car.returnFromRent();

    return carRepository.save(car);
  }

  @Transactional
  public Car sendToMaintenance(Long carId) {
    log.info("Send to maintenance: carId={}", carId);

    Car car = getCarById(carId);

    car.sendToMaintenance();

    return carRepository.save(car);
  }

  @Transactional
  public Car completeMaintenance(Long carId) {
    log.info("Complete maintenance: carId={}", carId);

    Car car = getCarById(carId);

    car.completeMaintenance();

    return carRepository.save(car);
  }

  @Transactional
  public void deleteCar(Long id) {
    log.info("Deleting car id={}", id);

    Car car = getCarById(id);
    carRepository.delete(car);
  }
}
