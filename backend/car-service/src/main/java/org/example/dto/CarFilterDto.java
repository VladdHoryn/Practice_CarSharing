package org.example.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CarFilterDto {
    private String brand;
    private String model;
    private Integer minYear;
    private Integer maxYear;
    private String carClass;      // ECONOMY, COMFORT, BUSINESS, LUXURY
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String status;        // AVAILABLE, RENTED, MAINTENANCE
    private String locationCity;
}
