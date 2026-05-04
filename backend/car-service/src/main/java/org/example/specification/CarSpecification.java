package org.example.specification;

import org.example.domain.Car;
import org.example.dto.CarFilterDto;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class CarSpecification {

    public static Specification<Car> filterBy(CarFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getBrand() != null && !filter.getBrand().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("brand"), filter.getBrand()));
            }

            if (filter.getModel() != null && !filter.getModel().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("model"), "%" + filter.getModel() + "%"));
            }

            if (filter.getMinYear() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("year"), filter.getMinYear()));
            }

            if (filter.getMaxYear() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("year"), filter.getMaxYear()));
            }

            if (filter.getCarClass() != null && !filter.getCarClass().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("carClass"), filter.getCarClass()));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("pricePerDay"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("pricePerDay"), filter.getMaxPrice()));
            }

            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getLocationCity() != null && !filter.getLocationCity().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("locationCity"), filter.getLocationCity()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
