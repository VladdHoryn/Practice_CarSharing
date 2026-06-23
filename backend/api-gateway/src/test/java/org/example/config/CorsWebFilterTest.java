package org.example.config;

import org.example.ApiGatewayServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        classes = ApiGatewayServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CorsWebFilterTest {

    @Autowired private WebTestClient webTestClient;

    @Test
    void corsHeaders_allowedOrigin_shouldReturnCorsHeaders() {
        webTestClient
                .options()
                .uri("/car/v1/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectHeader()
                .valueEquals("Access-Control-Allow-Origin", "http://localhost:3000")
                .expectHeader()
                .exists("Access-Control-Allow-Methods");
    }

    @Test
    void corsHeaders_disallowedOrigin_shouldNotReturnCorsHeaders() {
        webTestClient
                .options()
                .uri("/car/v1/test")
                .header("Origin", "http://evil.com")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectHeader()
                .doesNotExist("Access-Control-Allow-Origin");
    }

    @Test
    void corsHeaders_deleteMethod_shouldBeAllowed() {
        webTestClient
                .options()
                .uri("/car/v1/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "DELETE")
                .exchange()
                .expectHeader()
                .valueEquals("Access-Control-Allow-Origin", "http://localhost:3000");
    }

    @Test
    void corsHeaders_putMethod_shouldBeAllowed() {
        webTestClient
                .options()
                .uri("/user/v1/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "PUT")
                .exchange()
                .expectHeader()
                .valueEquals("Access-Control-Allow-Origin", "http://localhost:3000");
    }

    @Test
    void corsHeaders_contentTypeHeader_shouldBeAllowed() {
        webTestClient
                .options()
                .uri("/booking/v1/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type")
                .exchange()
                .expectHeader()
                .exists("Access-Control-Allow-Headers");
    }

    @Test
    void corsHeaders_authorizationHeader_shouldBeAllowed() {
        webTestClient
                .options()
                .uri("/payment/v1/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization")
                .exchange()
                .expectHeader()
                .exists("Access-Control-Allow-Headers");
    }

    @Test
    void corsHeaders_maxAge_shouldBe3600() {
        webTestClient
                .options()
                .uri("/car/v1/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectHeader()
                .valueEquals("Access-Control-Max-Age", "3600");
    }

    @Test
    void corsHeaders_requestFromPort8080_shouldBeDenied() {
        webTestClient
                .options()
                .uri("/car/v1/test")
                .header("Origin", "http://localhost:8080")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectHeader()
                .doesNotExist("Access-Control-Allow-Origin");
    }

    @Test
    void corsConfig_allowedMethods_shouldContainCRUD() {
        webTestClient
                .options()
                .uri("/user/v1/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectHeader()
                .valueEquals("Access-Control-Allow-Origin", "http://localhost:3000");
    }

    @Test
    void corsConfig_allowCredentials_shouldBeTrue() {
        webTestClient
                .options()
                .uri("/booking/v1/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectHeader()
                .valueEquals("Access-Control-Allow-Credentials", "true");
    }
}
