package org.example.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingTest {

  @Autowired
  private RouteLocator routeLocator;

  @Test
  void shouldHaveFourRoutes() {
    List<String> routeIds = routeLocator.getRoutes()
      .map(route -> route.getId())
      .collectList()
      .block();

    assertThat(routeIds).hasSize(4);
  }

  @Test
  void allRoutesShouldHavePredicates() {
    routeLocator.getRoutes()
      .doOnNext(route ->
        assertThat(route.getPredicate()).isNotNull())
      .collectList()
      .block();
  }

  @Test
  void userServiceRoute_shouldHavePathPredicate() {
    routeLocator.getRoutes()
      .filter(route -> "user-service".equals(route.getId()))
      .next()
      .doOnNext(route ->
        assertThat(route.getPredicate().toString())
          .contains("/user/v1/"))
      .block();
  }

  @Test
  void bookingServiceRoute_shouldHavePathPredicate() {
    routeLocator.getRoutes()
      .filter(route -> "booking-service".equals(route.getId()))
      .next()
      .doOnNext(route ->
        assertThat(route.getPredicate().toString())
          .contains("/booking/v1/"))
      .block();
  }

  @Test
  void carServiceRoute_shouldHavePathPredicate() {
    routeLocator.getRoutes()
      .filter(route -> "car-service".equals(route.getId()))
      .next()
      .doOnNext(route ->
        assertThat(route.getPredicate().toString())
          .contains("/car/v1/"))
      .block();
  }

  @Test
  void paymentServiceRoute_shouldHavePathPredicate() {
    routeLocator.getRoutes()
      .filter(route -> "payment-service".equals(route.getId()))
      .next()
      .doOnNext(route ->
        assertThat(route.getPredicate().toString())
          .contains("/payment/v1/"))
      .block();
  }

  @Test
  void bookingServiceRoute_shouldHaveCorrectUri() {
    routeLocator.getRoutes()
      .filter(route -> "booking-service".equals(route.getId()))
      .next()
      .doOnNext(route ->
        assertThat(route.getUri().toString())
          .contains("localhost:9998"))
      .block();
  }

  @Test
  void carServiceRoute_shouldHaveCorrectUri() {
    routeLocator.getRoutes()
      .filter(route -> "car-service".equals(route.getId()))
      .next()
      .doOnNext(route ->
        assertThat(route.getUri().toString())
          .contains("localhost:9997"))
      .block();
  }

  @Test
  void paymentServiceRoute_shouldHaveCorrectUri() {
    routeLocator.getRoutes()
      .filter(route -> "payment-service".equals(route.getId()))
      .next()
      .doOnNext(route ->
        assertThat(route.getUri().toString())
          .contains("localhost:9996"))
      .block();
  }

  @Test
  void shouldNotHaveUnknownRoutes() {
    List<String> knownIds = List.of(
      "user-service", "booking-service", "car-service", "payment-service"
    );

    routeLocator.getRoutes()
      .doOnNext(route ->
        assertThat(knownIds).contains(route.getId()))
      .collectList()
      .block();
  }

  @Test
  void shouldContainUserServiceRoute() {
    List<String> routeIds = routeLocator.getRoutes()
      .map(route -> route.getId())
      .collectList()
      .block();

    assertThat(routeIds).contains("user-service");
  }

  @Test
  void shouldContainBookingServiceRoute() {
    List<String> routeIds = routeLocator.getRoutes()
      .map(route -> route.getId())
      .collectList()
      .block();

    assertThat(routeIds).contains("booking-service");
  }

  @Test
  void shouldContainCarServiceRoute() {
    List<String> routeIds = routeLocator.getRoutes()
      .map(route -> route.getId())
      .collectList()
      .block();

    assertThat(routeIds).contains("car-service");
  }

  @Test
  void shouldContainPaymentServiceRoute() {
    List<String> routeIds = routeLocator.getRoutes()
      .map(route -> route.getId())
      .collectList()
      .block();

    assertThat(routeIds).contains("payment-service");
  }

  @Test
  void userServiceRoute_shouldHaveCorrectUri() {
    routeLocator.getRoutes()
      .filter(route -> "user-service".equals(route.getId()))
      .next()
      .doOnNext(route -> assertThat(route.getUri().toString())
        .contains("localhost:9999"))
      .block();
  }
}
