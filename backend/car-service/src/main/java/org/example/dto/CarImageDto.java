package org.example.dto;

import lombok.Data;
import java.util.List;

@Data
public class CarImageDto {
    private Long carId;
    private List<String> images;
    private String primaryImage;
}
