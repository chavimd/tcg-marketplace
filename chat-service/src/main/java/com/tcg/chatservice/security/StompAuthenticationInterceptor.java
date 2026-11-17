package com.tcg.chatservice.security;

import io.jsonwebtoken.JwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StompAuthenticationInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public StompAuthenticationInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            throw new MessagingException("Invalid STOMP frame");
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {
            return authenticate(message);
        } else {
            if (accessor.getUser() == null) {
                throw new MessagingException("STOMP connection is not authenticated");
            }
            if (accessor.getCommand() == StompCommand.SEND) {
                String destination = accessor.getDestination();
                if (destination == null || !destination.matches("^/app/trades/[1-9][0-9]*/messages$")) {
                    throw new MessagingException("Unsupported chat destination");
                }
            }
            if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
                String destination = accessor.getDestination();
                if (destination == null || !destination.matches("^/user/queue/trades/[1-9][0-9]*$")) {
                    throw new MessagingException("Unsupported subscription destination");
                }
            }
        }
        return message;
    }

    private Message<?> authenticate(Message<?> message) {
        MessageHeaderAccessor mutableHeaders = MessageHeaderAccessor.getMutableAccessor(message);
        if (!(mutableHeaders instanceof StompHeaderAccessor accessor)) {
            throw new MessagingException("Invalid STOMP CONNECT frame");
        }
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new MessagingException("Bearer token is required");
        }
        try {
            String token = header.substring(7);
            var claims = jwtService.parse(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            Number uidClaim = claims.get("uid", Number.class);
            Long userId = uidClaim == null ? null : uidClaim.longValue();
            if (username == null || username.isBlank() || role == null || userId == null
                    || !List.of("CLIENTE", "ADMIN").contains(role)) {
                throw new MessagingException("Invalid bearer token");
            }
            var authentication = new UsernamePasswordAuthenticationToken(
                    username, token, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            authentication.setDetails(userId);
            accessor.setUser(authentication);
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        } catch (JwtException | IllegalArgumentException exception) {
            throw new MessagingException("Invalid or expired bearer token", exception);
        }
    }
}
