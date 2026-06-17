package org.example.dto;

public package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.CarStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarSummaryResponse {
    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private String carClass;
    private Float pricePerDay;
    private Long userId;
    private CarStatus status;
    private String locationCity;
    private byte[] mainImage;  // тільки головне фото
} {
    
}
