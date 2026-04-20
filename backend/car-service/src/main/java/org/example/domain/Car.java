package org.example.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cars")
@Getter
@Setter
@NoArgsConstructor
public class Car {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public String brand;

  public String model;

  public Integer year;

  public CarClass carClass;

  public Float pricePerDay;

  public Long userId;

  public CarStatus status;

  public String imageUrl;
}
