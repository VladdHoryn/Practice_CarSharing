package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayServiceApplicationTest {

  @Test
  void contextLoads() {
    // перевіряє що Spring контекст піднімається без помилок
  }
}
