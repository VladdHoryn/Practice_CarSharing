package org.example.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${app.feign.user-service-url}", path = "/user/v1")
public interface UserServiceClient {

    @GetMapping("/analytics/admin/active/count")
    ResponseEntity<Long> countActiveUsers(@RequestHeader(HttpHeaders.AUTHORIZATION) String token);

    @GetMapping("/analytics/admin/roles/count")
    ResponseEntity<Long> countUsersByRole(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam("role") String role);
}
