package com.tcg.chatservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class StompAuthenticationInterceptorTests {

    private static final String SECRET = "tcg-local-development-secret-change-me";
    private final StompAuthenticationInterceptor interceptor =
            new StompAuthenticationInterceptor(new JwtService(SECRET));

    @Test
    void authenticatesStompConnectWithValidBearerToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer " + validToken());
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> authenticated = interceptor.preSend(message, mock(MessageChannel.class));

        StompHeaderAccessor authenticatedAccessor =
                MessageHeaderAccessor.getAccessor(authenticated, StompHeaderAccessor.class);
        assertEquals("collector", authenticatedAccessor.getUser().getName());
        assertEquals(7L, ((org.springframework.security.core.Authentication) authenticatedAccessor.getUser()).getDetails());
    }

    @Test
    void rejectsConnectWithoutBearerToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(MessagingException.class,
                () -> interceptor.preSend(message, mock(MessageChannel.class)));
    }

    private String validToken() {
        return Jwts.builder()
                .subject("collector")
                .claim("uid", 7L)
                .claim("role", "CLIENTE")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
