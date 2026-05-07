package org.example.service;

import org.example.domain.Car;
import org.example.dto.CarFilterDto;
import org.example.repository.CarRepository;
import org.example.specification.CarSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Page<Car> getFilteredCars(CarFilterDto filter, Pageable pageable) {
        return carRepository.findAll(CarSpecification.filterBy(filter), pageable);
    }

    public List<Car> getCarsByBrand(String brand) {
        return carRepository.findAll((root, query, cb) -> 
            cb.equal(root.get("brand"), brand));
    }

    public List<Car> getAvailableCars() {
        return carRepository.findAll((root, query, cb) -> 
            cb.equal(root.get("status"), "AVAILABLE"));
    }

    public List<Car> getCarsByClass(String carClass) {
        return carRepository.findAll((root, query, cb) -> 
            cb.equal(root.get("carClass"), carClass));
    }
}
