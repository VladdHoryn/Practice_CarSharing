package org.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.test.StepVerifier;

class ApiGatewayServiceApplicationTest {

    private final ApiGatewayServiceApplication application = new ApiGatewayServiceApplication();

    @Nested
    @DisplayName("Bean creation")
    class BeanCreationTests {

        @Test
        void createsCorsWebFilter() {
            CorsWebFilter filter = application.corsWebFilter();

            assertThat(filter).isNotNull();
        }

        @Test
        void createsDeduplicationFilter() {
            WebFilter filter = application.corsDeduplicationFilter();

            assertThat(filter).isNotNull();
        }
    }

    @Nested
    @DisplayName("CORS deduplication filter")
    class CorsDeduplicationFilterTests {

        private final WebFilter filter = application.corsDeduplicationFilter();

        @Test
        void deduplicatesAllowOriginHeader() {

            MockServerWebExchange exchange =
                    MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

            ServerHttpResponse response = exchange.getResponse();

            response.getHeaders()
                    .addAll(
                            HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                            List.of("http://localhost:3000", "http://localhost:3000"));

            WebFilterChain chain = ex -> ex.getResponse().setComplete();

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            List<String> values =
                    response.getHeaders().get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);

            assertThat(values).containsExactly("http://localhost:3000");
        }

        @Test
        void deduplicatesAllowCredentialsHeader() {

            MockServerWebExchange exchange =
                    MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

            ServerHttpResponse response = exchange.getResponse();

            response.getHeaders()
                    .addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, List.of("true", "true"));

            WebFilterChain chain = ex -> ex.getResponse().setComplete();

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            List<String> values =
                    response.getHeaders().get(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);

            assertThat(values).containsExactly("true");
        }

        @Test
        void keepsSingleOriginValueUntouched() {

            MockServerWebExchange exchange =
                    MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

            exchange.getResponse()
                    .getHeaders()
                    .set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000");

            WebFilterChain chain = ex -> ex.getResponse().setComplete();

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            assertThat(
                            exchange.getResponse()
                                    .getHeaders()
                                    .get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                    .containsExactly("http://localhost:3000");
        }

        @Test
        void doesNothingWhenHeadersMissing() {

            MockServerWebExchange exchange =
                    MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

            WebFilterChain chain = ex -> ex.getResponse().setComplete();

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            assertThat(
                            exchange.getResponse()
                                    .getHeaders()
                                    .get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                    .isNull();
        }

        @Test
        void invokesNextFilterInChain() {

            MockServerWebExchange exchange =
                    MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

            boolean[] called = {false};

            WebFilterChain chain =
                    ex -> {
                        called[0] = true;
                        return ex.getResponse().setComplete();
                    };

            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            assertThat(called[0]).isTrue();
        }
    }
}
