package org.example.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
  name = "car-service",
  url = "${app.feign.car-service-url}",
  path = "/cars/v1"
)
public interface CarServiceClient {

  @GetMapping("/analytics/owners/{ownerId}/cars/count")
  ResponseEntity<Long> countCarsByOwnerId(@PathVariable("ownerId") Long ownerId);
}
