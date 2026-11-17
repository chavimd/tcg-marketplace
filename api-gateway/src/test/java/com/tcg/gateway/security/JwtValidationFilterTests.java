package com.tcg.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtValidationFilterTests {

    private static final String SECRET = "tcg-local-development-secret-change-me";
    private final JwtValidationFilter filter = new JwtValidationFilter(SECRET);

    @Test
    void rejectsPrivateRouteWithoutToken() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/trades").build());
        AtomicBoolean forwarded = new AtomicBoolean();
        GatewayFilterChain chain = request -> {
            forwarded.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertFalse(forwarded.get());
    }

    @Test
    void forwardsPrivateRouteWithValidToken() {
        String token = Jwts.builder()
                .subject("collector")
                .claim("uid", 7L)
                .claim("role", "CLIENTE")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        var request = MockServerHttpRequest.get("/api/trades")
                .header("Authorization", "Bearer " + token)
                .build();
        var exchange = MockServerWebExchange.from(request);
        AtomicBoolean forwarded = new AtomicBoolean();

        filter.filter(exchange, chain -> {
            forwarded.set(true);
            return Mono.empty();
        }).block();

        assertTrue(forwarded.get());
    }

    @Test
    void allowsPublicCatalogReadsWithoutToken() {
        for (String path : new String[]{"/api/cards", "/api/games", "/api/publications"}) {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
            AtomicBoolean forwarded = new AtomicBoolean();

            filter.filter(exchange, chain -> {
                forwarded.set(true);
                return Mono.empty();
            }).block();

            assertTrue(forwarded.get(), "debería ser público: " + path);
        }
    }

    @Test
    void requiresTokenForCatalogWrites() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/cards").build());
        AtomicBoolean forwarded = new AtomicBoolean();

        filter.filter(exchange, chain -> {
            forwarded.set(true);
            return Mono.empty();
        }).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertFalse(forwarded.get());
    }
}
