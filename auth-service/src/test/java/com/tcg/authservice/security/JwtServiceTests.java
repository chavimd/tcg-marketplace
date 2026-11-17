package com.tcg.authservice.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTests {

    private final JwtService jwtService = new JwtService(
            "a-test-secret-that-is-at-least-thirty-two-bytes", 3600);

    @Test
    void generatedTokenContainsUsernameAndExpiration() {
        var claims = jwtService.parseToken(jwtService.generateToken(7L, "collector", "CLIENTE"));

        assertEquals("collector", claims.getSubject());
        assertEquals(7L, claims.get("uid", Long.class));
        assertEquals("CLIENTE", claims.get("role", String.class));
        assertEquals(3600, (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000);
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String token = new JwtService("another-test-secret-that-is-also-over-thirty-two-bytes", 3600)
                .generateToken(7L, "collector", "CLIENTE");

        assertThrows(JwtException.class, () -> jwtService.parseToken(token));
    }

    @Test
    void rejectsShortSigningSecret() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("short", 3600));
    }
}
