package com.tcg.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTests {

    private final CorsConfig corsConfig = new CorsConfig();

    @Test
    void allowsLocalFrontendOriginAndApiHeaders() {
        var source = corsConfig.corsConfigurationSource("http://localhost:5500");
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/auth/login")
                        .header("Origin", "http://localhost:5500")
                        .build());
        CorsConfiguration configuration = source.getCorsConfiguration(exchange);

        assertNotNull(configuration);
        assertEquals("http://localhost:5500", configuration.checkOrigin("http://localhost:5500"));
        assertTrue(configuration.checkHttpMethod(HttpMethod.POST).contains(HttpMethod.POST));
        assertEquals(List.of("authorization", "content-type"),
                configuration.checkHeaders(List.of("authorization", "content-type")));
    }

    @Test
    void rejectsUnconfiguredOrigin() {
        var source = corsConfig.corsConfigurationSource("http://localhost:5500");
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/auth/login")
                        .header("Origin", "https://example.invalid")
                        .build());

        assertNull(source.getCorsConfiguration(exchange).checkOrigin("https://example.invalid"));
    }
}
