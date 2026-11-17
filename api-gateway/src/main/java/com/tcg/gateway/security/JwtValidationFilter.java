package com.tcg.gateway.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private final SecretKey key;

    public JwtValidationFilter(@Value("${app.jwt.secret}") String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes long");
        }
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }
        String path = request.getURI().getPath();
        if (isPublic(path, request.getMethod())) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }
        try {
            var claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(authorization.substring(7)).getPayload();
            String subject = claims.getSubject();
            String role = claims.get("role", String.class);
            Number uidClaim = claims.get("uid", Number.class);
            if (subject == null || subject.isBlank() || role == null || uidClaim == null
                    || !List.of("CLIENTE", "ADMIN").contains(role)) {
                return unauthorized(exchange);
            }
            return chain.filter(exchange);
        } catch (JwtException | IllegalArgumentException exception) {
            return unauthorized(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublic(String path, HttpMethod method) {
        if (path.startsWith("/actuator/health") || path.equals("/actuator/info")) {
            return true;
        }
        if (path.equals("/api/auth/register") || path.equals("/api/auth/login")) {
            return method == HttpMethod.POST;
        }
        return (method == HttpMethod.GET
                && (path.equals("/api/cards") || path.startsWith("/api/cards/")
                    || path.equals("/api/games")
                    || path.equals("/api/publications") || path.startsWith("/api/publications/")))
                || path.equals("/ws") || path.startsWith("/ws/");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
